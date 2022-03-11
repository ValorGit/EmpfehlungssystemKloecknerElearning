package empfehlungssystem;
import org.neo4j.driver.internal.shaded.reactor.util.annotation.Nullable;

/* Objekt vom Typ Kurs */
public class Kurs {

    private String name;
    private String beschreibung;
    private String bild;
    private String typ;
    private int niveau;

    public Kurs(String name, @Nullable String beschreibung, @Nullable String bild, @Nullable int niveau, String typ) {
        this.name = name;
        this.beschreibung = beschreibung;
        this.niveau = niveau;
        this.bild = bild;
        this.typ = typ;
    }

    public String getName() {
        return name;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public String getBild() {
        return bild;
    }

    public int getNiveau() {
        return niveau;
    }

    public String getTyp() {
        return typ;
    }

    @Override
    public String toString() {
        return getName() + "Beschreibung:" + getBeschreibung();
    }

}
