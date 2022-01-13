package cz.incad.kramerius.services;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TestLogger {


    public static class Osoba {
        private String jmeno;
        private String prijmeni;


        public Osoba(String jmeno, String prijmeni) {
            this.jmeno = jmeno;
            this.prijmeni = prijmeni;
        }

        public String getPrijmeni() {
            return prijmeni;
        }

        public String getJmeno() {
            return jmeno;
        }

        @Override
        public String toString() {
            return "Osoba{" +
                    "jmeno='" + jmeno + '\'' +
                    ", prijmeni='" + prijmeni + '\'' +
                    '}';
        }
    }


}

