package mg.itu.tommy.util;

public class Mapfile {
    private byte[] bytes;
    private String extension;
    private String nom;

    public Mapfile() {}

    public byte[] getBytes() { return bytes; }
    public void setBytes(byte[] bytes) { this.bytes = bytes; }

    public String getExtension() { return extension; }
    public void setExtension(String extension) { this.extension = extension; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
}