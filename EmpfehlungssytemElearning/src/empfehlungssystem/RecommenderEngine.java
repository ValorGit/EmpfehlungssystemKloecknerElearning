package empfehlungssystem;

import org.neo4j.driver.*;
import org.neo4j.driver.Record;
import org.neo4j.driver.exceptions.Neo4jException;
import org.neo4j.driver.types.Node;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.neo4j.driver.Config.TrustStrategy.trustAllCertificates;

/**
 * Umfasst die Programmlogik für das Empfehlungssystem + Helfermethoden um
 * Arbeitsschritte zu erleichtern
 */
public class RecommenderEngine implements AutoCloseable {
    private static final Logger LOGGER = Logger.getLogger(RecommenderEngine.class.getName());
    private final Driver driver;
    private final String url = "neo4j+s://ad5badef.databases.neo4j.io";
    private final String userCurrent = "neo4j";
    private final String passwordCurrent = "JOgvjfXTDzlBxHK42FvqroNlC5A6DbkGRCP6X8BJtJs";

    // Festgelegt vorher: public RecommenderEngine(String uri, String user, String
    // password, Config config)
    public RecommenderEngine() {
        /* Startet den Treiber */
        driver = GraphDatabase.driver(url, AuthTokens.basic(userCurrent, passwordCurrent), Config.defaultConfig());
    }

    /* Schließt den Treiber */
    @Override
    public void close() throws Exception {
        driver.close();
    }

    /* Setzt einen Kurs auf den Status abgeschlossen */
    public void kursAbgeschlossen(final String LernerName, final String abgeschlossenerKurs) {

        String kursAbgeschlossenQuery = "match (l:Lerner), (k:Kurs)\n" +
                "where l.name = $Lerner_name And k.name = $Kurs_name\n" +
                "merge (l)-[r:HAT_BESUCHT]->(k)\n" +
                "return r, l, k";

        Map<String, Object> params = new HashMap<>();
        params.put("Lerner_name", LernerName);
        params.put("Kurs_name", abgeschlossenerKurs);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(kursAbgeschlossenQuery, params);
                return result.single();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, kursAbgeschlossenQuery + " raised an exception", ex);
            throw ex;
        }
    }

    /* Legt einen neuer Benutzer in der Datenbank an */
    public void neuerUser(String name, String email, String position, List<String> skills) {
        String neuerUserQuery = "merge (l:Lerner {email: $email})\n" +
                "On Create set l.name = $name \n" +
                "with l \n" +
                "match (p:Position {name: $position}) \n" +
                "match (ut:Subthema) \n" +
                "merge (l)-[r:HAT_POSITION]->(p)\n" +
                "merge (l)-[:HAT_KOMPETENZ {gewichtung: 3}]->(ut)\n" +
                "return l, p";

        Map<String, Object> params = new HashMap<>();
        params.put("name", name);
        params.put("email", email);
        params.put("position", position);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            List<Record> record = session.writeTransaction(tx -> {
                Result result = tx.run(neuerUserQuery, params);
                return result.list();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, neuerUserQuery + " raised an exception", ex);
            throw ex;
        }
        this.userSkillZuweisung(name, skills);

    }

    /* Erstellt eine Relation Lerner-[:LERNT_SKILL]->Skill */
    public void userSkillZuweisung(String user, List<String> skills) {
        this.userSkillLoeschen(user);

        String userSkillQuery = "match (l:Lerner {name: $user})\n" +
                " Unwind $skills AS gewaehlte match (s:Skill {name: gewaehlte}) merge (l)-[:LERNT_SKILL]->(s) return l";

        Map<String, Object> params = new HashMap<>();
        params.put("user", user);
        params.put("skills", skills);

        try (Session session = driver.session()) {

            // Write transactions allow the driver to handle retries and transient errors
            List<Record> record = session.writeTransaction(tx -> {
                Result result = tx.run(userSkillQuery, params);
                return result.list();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, userSkillQuery + " raised an exception", ex);
            throw ex;
        }

    }

    /*
     * Löscht Relationen vom Typ [:LERNT_SKILL] ausgehend vom angegebenen Benutzer
     */
    public void userSkillLoeschen(String user) {
        String userSkillLoeschenQuery = "match (l:Lerner {name: $user})\n" +
                " match (s:Skill) match (l)-[r:LERNT_SKILL]->(s) delete r";

        Map<String, Object> params = new HashMap<>();
        params.put("user", user);

        try (Session session = driver.session()) {

            // Write transactions allow the driver to handle retries and transient errors
            List<Record> record = session.writeTransaction(tx -> {
                Result result = tx.run(userSkillLoeschenQuery, params);
                return result.list();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, userSkillLoeschenQuery + " raised an exception", ex);
            throw ex;
        }

    }

    /*
     * Weist einem Kurs ein Thema zu. Erstellt eine Relation vom Typ
     * Kurs-[:HAT_THEMA]->Subthema
     */
    public void hatThema(final String kurs, final String unterthema) {

        String hatThemaQuery = "match (k:Kurs {name: $kurs})\n" +
                "merge (t:Subthema {name: $unterthema})\n" +
                "merge (k)-[r:HAT_THEMA]->(t)\n" +
                "return t, k";

        Map<String, Object> params = new HashMap<>();
        params.put("unterthema", unterthema);
        params.put("kurs", kurs);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(hatThemaQuery, params);
                return result.single();
            });

            // You should capture any errors along with the query and data for traceability
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, hatThemaQuery + " raised an exception", ex);
            throw ex;
        }
    }

    // Weist ein Subthema ein Übergeordnetes Thema zu. Falls noch nicht vorhanden,
    // wird ein neues Thema angelegt
    public void istUnterthema(final String unterthema, final String themenbereich) {

        String istUnterthemaQuery = "match (st:Subthema {name: $unterthema})\n" +
                "merge (t:Themenbereich {name: $themenbereich})\n" +
                "merge (st)-[r:IST_UNTERTHEMA]->(t)\n" +
                "return t, st";

        Map<String, Object> params = new HashMap<>();
        params.put("unterthema", unterthema);
        params.put("themenbereich", themenbereich);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(istUnterthemaQuery, params);
                return result.single();
            });

            // You should capture any errors along with the query and data for traceability
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, istUnterthemaQuery + " raised an exception", ex);
            throw ex;
        }
    }

    /* Helfermethode um einen neune Kurs zu erstellen */
    public void erstelleKurs(final String KursName, final String beschreibung, final int niveau, final String link,
            final String medium, final String typ) {
        String erstelleKursQuery = "MERGE (k1:Kurs { name: $KursName }) \n" +
                "set k1.beschreibung = $beschreibung, k1.niveau = $niveau , k1.typ = $typ \n" +
                "RETURN k1";

        Map<String, Object> params = new HashMap<>();
        params.put("KursName", KursName);
        params.put("beschreibung", beschreibung);
        params.put("niveau", niveau);
        params.put("typ", typ);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(erstelleKursQuery, params);
                return result.single();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, erstelleKursQuery + " raised an exception", ex);
            throw ex;
        }

    }

    /* Prüft, ob der User in der Datenbank angelegt wurde */
    public String pruefeEmail(final String email) {
        String readLernerByNameQuery = "MATCH (l:Lerner)\n" +
                "WHERE l.email = $Lerner_name\n" +
                "RETURN l.name AS name";

        Map<String, Object> params = Collections.singletonMap("Lerner_name", email);

        String lerner;

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.readTransaction(tx -> {
                Result result = tx.run(readLernerByNameQuery, params);
                return result.single();
            });

            lerner = record.get("name").asString();
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readLernerByNameQuery + " raised an exception", ex);
            throw ex;
        }
        return lerner;

    }

    /* Suche einen Lerner nach seinem Namen */
    public void findeLerner(final String LernerName) {
        String readLernerByNameQuery = "MATCH (l:Lerner)\n" +
                "WHERE l.name = $Lerner_name\n" +
                "RETURN l.name AS name";

        Map<String, Object> params = Collections.singletonMap("Lerner_name", LernerName);

        String lerner;

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.readTransaction(tx -> {
                Result result = tx.run(readLernerByNameQuery, params);
                return result.single();
            });

            lerner = record.get("name").asString();
        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readLernerByNameQuery + " raised an exception", ex);
            throw ex;
        }
    }

    /* Helfermethode, um einen neuen Skill zu erstellen */
    public void erstelleSkill(final String skill) {
        String erstelleSkillQuery = "merge (s:Skill {name: $skillname}) RETURN s";

        Map<String, Object> params = Collections.singletonMap("skillname", skill);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(erstelleSkillQuery, params);
                return result.single();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, erstelleSkillQuery + " raised an exception", ex);
            throw ex;
        }
    }

    /* Erstellt eine Relation vom Typ Kurs-[:FOERDERT]->Skill */
    public void erstelleSkillZuweisung(String kurs, String skill) {

        String skillZuweisungQuery = "match (k:Kurs {name: $kurs})\n" +
                "match (s:Skill {name: $skill})\n" +
                "merge (k)-[r:FOERDERT]->(s) return s";

        Map<String, Object> params = new HashMap<>();
        params.put("skill", skill);
        params.put("kurs", kurs);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(skillZuweisungQuery, params);
                return result.single();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, skillZuweisungQuery + " raised an exception", ex);
            throw ex;
        }

    }

    /*
     * Erstellt eine Zuweisung für einen notwendigen Skill für eine Position.
     * Erstellt eine Relation vom Typ Position-[:ERFORDERT_SKILL]-Skill
     */
    public void erstelleNotwendigeSkillZuweisung(String position, String skill) {

        String skillZuweisungQuery = "match (p:Position {name: $position})\n" +
                "match (s:Skill {name: $skill})\n" +
                "merge (p)-[r:ERFORDERT_SKILL]->(s) return p,s";

        Map<String, Object> params = new HashMap<>();
        params.put("skill", skill);
        params.put("position", position);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(skillZuweisungQuery, params);
                return result.single();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, skillZuweisungQuery + " raised an exception", ex);
            throw ex;
        }

    }

    /* Helfermethode, um Kurse aus einer CSV-Datei der Datenbank hinzuzufügen */
    public void erstelleKursAusCSV(String csvUrl) {
        List<List<String>> records = new ArrayList<>();

        try (BufferedReader csvReader = new BufferedReader(new FileReader(csvUrl))) {
            String row;
            while ((row = csvReader.readLine()) != null) {
                String bereinigt = row.replace("\"", "");
                String[] data = bereinigt.split(";");
                records.add(Arrays.asList(data));
            }
        } catch (IOException e) {
            System.out.println("Datei nicht gefunden");
        }

        for (int i = 0; i < records.size(); i++) {

            String kurs = records.get(i).get(0);
            String skills = records.get(i).get(1);
            String thema = records.get(i).get(2);
            String beschreibung = records.get(i).get(3);
            int niveau = Integer.parseInt(records.get(i).get(4));
            String typ = "";
            if (records.get(i).size() >= 6) {
                typ = records.get(i).get(5);
            }
            this.erstelleKurs(kurs, beschreibung, niveau, "", "", typ);

            this.hatThema(kurs, thema);
            StringTokenizer stk = new StringTokenizer(skills, ",");

            while (stk.hasMoreTokens()) {

                this.erstelleSkillZuweisung(kurs, stk.nextToken());
            }

        }

    }

    /* Ausgabe anhand eines berechneten Empfehlwerts */
    public List<Record> empfehleKurs(String lerner) {
        String readLernerByNameQuery = "match (l:Lerner {name: $lerner})-[e:EMPFEHLE]-(k:Kurs) match (l)-[r2:HAT_KOMPETENZ]->(ut:Subthema)<-[:HAT_THEMA]-(k) WHERE r2.kompetenzniveau>= k.niveau AND Not (l)-[:HAT_BESUCHT]->(k) return k, e.empfehlwert As empfehlwert Order by empfehlwert desc limit 5";

        List<Record> record;
        Map<String, Object> params = Collections.singletonMap("lerner", lerner);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            record = session.readTransaction(tx -> {
                Result result = tx.run(readLernerByNameQuery, params);
                return result.list();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readLernerByNameQuery + " raised an exception", ex);
            throw ex;
        }
        return record;
    }

    /* Empfehle Kurse passend zum Skillset */
    public List<Record> empfehleKursSkills(String lerner) {
        String readLernerByNameQuery = "match (l:Lerner {name: $lerner})-[:LERNT_SKILL]->(s:Skill) with l, collect(id(s)) as lSkills match (k:Kurs )-[:FOERDERT]->(s:Skill)<-[:LERNT_SKILL]-(l) match (l)-[r2:HAT_KOMPETENZ]->(ut:Subthema)<-[:HAT_THEMA]-(k) WHERE r2.gewichtung>= k.niveau AND Not (l)-[:HAT_BESUCHT]->(k) with l, lSkills, k match (k)-[:FOERDERT]->(s2:Skill) with l, lSkills,k, toFLoat(size(apoc.coll.intersection(lSkills, collect(id(s2))))) / toFloat(size(apoc.coll.union(lSkills, collect(id(s2))))) as sim  return k, sim ORDER By sim desc limit 5";

        List<Record> record;
        Map<String, Object> params = Collections.singletonMap("lerner", lerner);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            record = session.readTransaction(tx -> {
                Result result = tx.run(readLernerByNameQuery, params);
                return result.list();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readLernerByNameQuery + " raised an exception", ex);
            throw ex;
        }
        return record;
    }

    /* Empfehle Kurse passend zur aktuellen Position */
    public List<Record> empfehleKursPosition(String lerner) {
        String readLernerByNameQuery = "match (l:Lerner{name: $lerner})-[pr:HAT_POSITION]->(p:Position) match (k2:Kurs)-[relevanz:RELEVANT_FUER]->(p)match (l)-[r2:HAT_KOMPETENZ]->(ut:Subthema)<-[:HAT_THEMA]-(k2) WHERE r2.gewichtung>= k2.niveau AND Not (l)-[:HAT_BESUCHT]->(k2) return k2, relevanz.gewichtung ORDER BY relevanz.gewichtung desc limit 5";

        List<Record> record;
        Map<String, Object> params = Collections.singletonMap("lerner", lerner);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            record = session.readTransaction(tx -> {
                Result result = tx.run(readLernerByNameQuery, params);
                return result.list();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readLernerByNameQuery + " raised an exception", ex);
            throw ex;
        }
        return record;
    }

    /*
     * Berecht die Relevanz eines Kurses für eine Position. Erstellt eine Relation
     * vom Typ Kurs-[:RELEVANT_FUER]->Position
     */
    public void berechneRelevanzPosition(String position) {

        String berechneRelevanzQuery = "match (p:Position {name: $position}) match (k:Kurs) match (s:Skill) match (ut:Subthema)  match (p)-[r1:ERFORDERT_THEMA]->(ut)<-[:HAT_THEMA]-(k) merge (k)-[:RELEVANT_FUER {gewichtung: r1.gewichtung}]->(p)";

        Map<String, Object> params = Collections.singletonMap("position", position);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(berechneRelevanzQuery, params);
                return result.single();
            });

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, berechneRelevanzQuery + " raised an exception", ex);
            throw ex;
        }

    }

    /* Gibt die aktuelle Position des Benutzers aus */
    public String gibaktuellePosition(final String lernerName) {

        String aktuellePosition = "";
        String readLernerByNameQuery = "MATCH (l:Lerner {name: $lernerName}) match (p:Position) match (l)-[:HAT_POSITION]->(p) return p.name as name";

        Map<String, Object> params = Collections.singletonMap("lernerName", lernerName);

        try (Session session = driver.session()) {
            Record record = session.readTransaction(tx -> {
                Result result = tx.run(readLernerByNameQuery, params);
                return result.single();
            });
            aktuellePosition = record.get("name").asString();

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readLernerByNameQuery + " raised an exception", ex);
            throw ex;
        }
        return aktuellePosition;
    }

    /* Gibt alle bisher angelegten Skills aus */
    public List<String> gibAlleSkills() {
        String readSkillsQuery = "match (s: Skill) with s ORDER by s.name  return collect(s.name)  AS skills";
        List<String> skills = new ArrayList<String>();

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.readTransaction(tx -> {
                Result result = tx.run(readSkillsQuery);
                return result.single();
            });
            for (int i = 0; i < record.get("skills").size(); i++) {
                skills.add(record.get("skills").get(i).asString());

            }

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readSkillsQuery + " raised an exception", ex);
            throw ex;
        }
        return skills;
    }

    /* Gibt die aktuellen Lernziele des Benutzers aus */
    public List<String> gibLernerSkills(String lerner) {
        String readSkillsQuery = "match (l:Lerner {name : $lerner}) match (s: Skill) match (l)-[:LERNT_SKILL]->(s) with s ORDER by s.name  return collect(s.name)  AS skills";
        List<String> skills = new ArrayList<String>();

        Map<String, Object> params = Collections.singletonMap("lerner", lerner);

        try (Session session = driver.session()) {
            Record record = session.readTransaction(tx -> {
                Result result = tx.run(readSkillsQuery, params);
                return result.single();
            });
            for (int i = 0; i < record.get("skills").size(); i++) {
                skills.add(record.get("skills").get(i).asString());

            }

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readSkillsQuery + " raised an exception", ex);
            throw ex;
        }
        return skills;
    }

    /* Gibt die Jobfamilie des Lernenden aus */
    public String gibJobfamilie(String lerner) {
        String jobfamilie;
        String gibJobfamilieQuery = "match (l:Lerner {name: $lerner}) match (l)-[:HAT_POSITION]->(:Position)-[:HAT_JOBFAMILIE]->(j:Jobfamilie) return j";

        Map<String, Object> params = Collections.singletonMap("lerner", lerner);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(gibJobfamilieQuery, params);
                return result.single();
            });
            jobfamilie = record.get("j").get("name").asString();

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, gibJobfamilieQuery + " raised an exception", ex);
            throw ex;
        }
        return jobfamilie;

    }

    /* Gibt den Kurs des Monats aus der Jobfamile */
    public Kurs gibKursDesMonats(String lerner) {
        String jobfamilie = this.gibJobfamilie(lerner);
        Kurs kursDesMonats;
        String gibKursDesMonats = "match (l:Lerner)-[:HAT_POSITION]->(p:Position)-[:HAT_JOBFAMILIE]->(j:Jobfamilie {name: $jobfamilie}) with l match (l)-[b:HAT_BESUCHT]->(k) return count(b) as besuche, k ORDER BY besuche Desc limit 1";

        Map<String, Object> params = Collections.singletonMap("jobfamilie", jobfamilie);

        try (Session session = driver.session()) {
            // Write transactions allow the driver to handle retries and transient errors
            Record record = session.writeTransaction(tx -> {
                Result result = tx.run(gibKursDesMonats, params);
                return result.single();
            });
            kursDesMonats = new Kurs(record.get("k").get("name").asString(),
                    record.get("k").get("beschreibung").asString(),
                    record.get("k").get("bildUrl").asString(), 0, record.get("k").get("typ").asString());

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, gibKursDesMonats + " raised an exception", ex);
            throw ex;
        }
        return kursDesMonats;

    }

    /* Gibt alle bisher angelegten Positionen aus */
    public List<String> gibAllePositionen() {
        String readPositionenQuery = "match (p: Position) with p ORDER by p.name  return collect(p.name)  AS positionen";
        List<String> positionen = new ArrayList<String>();

        try (Session session = driver.session()) {
            Record record = session.readTransaction(tx -> {
                Result result = tx.run(readPositionenQuery);
                return result.single();
            });
            for (int i = 0; i < record.get("positionen").size(); i++) {
                positionen.add(record.get("positionen").get(i).asString());

            }

        } catch (Neo4jException ex) {
            LOGGER.log(Level.SEVERE, readPositionenQuery + " raised an exception", ex);
            throw ex;
        }
        return positionen;
    }

}