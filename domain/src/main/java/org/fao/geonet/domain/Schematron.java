package org.fao.geonet.domain;

import javax.persistence.*;
import java.io.File;
import java.util.Map;

/**
 * An entity representing a schematron. It contains the file to the schematron
 * definition, the schema it belongs and if it is required or just a
 * recommendation.
 * 
 * @author delawen
 */
@Entity
@Table(name = "schematron",
        uniqueConstraints = @UniqueConstraint(columnNames = {"schemaName","file"}))
@Cacheable
@Access(AccessType.PROPERTY)
@SequenceGenerator(name= Schematron.ID_SEQ_NAME, initialValue=100, allocationSize=1)
public class Schematron extends Localized {
    static final String ID_SEQ_NAME = "schematron_id_seq";

	private int id;
	private String schemaName;
	private String file;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = ID_SEQ_NAME)
	@Column(nullable = false)
	public int getId() {
		return id;
	}

	public Schematron setId(int id) {
		this.id = id;
		return this;
	}

	@Override
	public String toString() {
		return "Schematron [_id=" + id + ", isoschema=" + schemaName + ", file="
				+ file + ", description"
				+ getLabelTranslations() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Schematron other = (Schematron) obj;
		if (id != other.id)
			return false;
		return true;
	}

	/**
	 * @return the schema
	 */
	@Column(nullable = false, name = "schemaName")
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @param schemaName
	 *            the schema to set
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * @return the file
	 */
	@Column(nullable = false, name = "file")
	public String getFile() {
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(String file) {
		this.file = file;
	}

    @Override
    @ElementCollection(fetch = FetchType.LAZY, targetClass = String.class)
    @CollectionTable(joinColumns = @JoinColumn(name = "idDes"), name = "SchematronDes")
    @MapKeyColumn(name = "langId", length = 5)
    @Column(name = "label", nullable = false, length = 96)
    public Map<String, String> getLabelTranslations() {
        return super.getLabelTranslations();
    }
    private final static int EXTENSION_LENGTH = ".xsl".length();
    private final static String SEPARATOR = File.separator;
    private final static String ALT_SEPARATOR;

    static {
        if (SEPARATOR.equals("\\")) {
            ALT_SEPARATOR = "/";
        } else {
            ALT_SEPARATOR = "\\";
        }
    }

    @Transient
    public String getRuleName() {
        if (file == null) {
            return "unnamed rule";
        }
        int lastSegmentIndex = file.lastIndexOf(SEPARATOR);
        if (lastSegmentIndex < 0) {
            lastSegmentIndex = file.lastIndexOf(ALT_SEPARATOR);
        }

        if (lastSegmentIndex < 0) {
            lastSegmentIndex = 0;
        } else {
            // drop the separator character
            lastSegmentIndex += 1;
        }

        String rule = file.substring(lastSegmentIndex, file.length() - EXTENSION_LENGTH);
        String lowerCaseRuleName = rule.toLowerCase();
        for (SchematronRequirement requirement : SchematronRequirement.values()) {
            if (lowerCaseRuleName.endsWith("."+requirement.name().toLowerCase())) {
                return rule.substring(0, rule.length() - requirement.name().length() - 1);
            }
        }
        return rule;
    }

    @Transient
    public SchematronRequirement getDefaultRequirement() {
        final String lowerCaseFile = getFile().toLowerCase();
        for (SchematronRequirement requirement : SchematronRequirement.values()) {
            if (lowerCaseFile.endsWith("."+requirement.name().toLowerCase() + ".xsl")) {
                return requirement;
            }
        }
        return SchematronRequirement.REQUIRED;
    }
}
