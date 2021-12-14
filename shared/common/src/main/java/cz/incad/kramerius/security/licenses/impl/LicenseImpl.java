package cz.incad.kramerius.security.licenses.impl;

import cz.incad.kramerius.security.licenses.License;

import java.io.Serializable;

public class LicenseImpl implements License, Serializable {

    private int id = -1;
    private String name;
    private String group;
    private String description;
    private int labelPrirority = DEFAULT_PRIORITY;


    public LicenseImpl(int id, String name, String description, String group, int labelPrirority) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.group = group;
        this.labelPrirority = labelPrirority;
        //validateName(name);
    }

    public LicenseImpl(String name, String description, String group, int labelPrirority) {
        this.name = name;
        this.description = description;
        this.group = group;
        this.labelPrirority = labelPrirority;
        //validateName(name);
    }

    public LicenseImpl(String name, String description, String group) {
        this.description = description;
        this.name = name;
        this.group = group;
        //validateName(name);
    }

    public LicenseImpl(int id, String name, String description, String group) {
        this.id = id;
        this.description = description;
        this.name = name;
        this.group = group;
        //validateName(name);
    }

    private void validateName(String name) {
        if (!License.ACCEPTABLE_LABEL_NAME_REGEXP.matcher(name).matches()) {
            throw new IllegalArgumentException("Label name must contain only a characters, digits and following set of chars [./-_:]");
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int getPriority() {
        return this.labelPrirority;
    }

    @Override
    public License getUpdatedPriorityLabel(int pr) {
        return new LicenseImpl(this.id, this.name, this.description, this.group, pr);
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public String getGroup() {
        return group;
    }


    @Override
    public String getDescription() {
        return this.description;
    }



    @Override
    public String toString() {
        if (this.id != -1) {
            return "LabelImpl{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", group='" + group + '\'' +
                    ", labelPrirority=" + labelPrirority +
                    '}';

        } else {
            return "LabelImpl{" +
                    ", name='" + name + '\'' +
                    ", group='" + group + '\'' +
                    ", labelPrirority=" + labelPrirority +
                    '}';
        }
    }
}
