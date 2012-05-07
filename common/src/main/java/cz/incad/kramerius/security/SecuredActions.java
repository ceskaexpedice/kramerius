/*
 * Copyright (C) 2010 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.kramerius.security;

/**
 * Vyctovy typ chranenych akci systemu K4 
 */
public enum SecuredActions {

    /** Akce cist */
    READ("read"),
    
    /** Akce pro proces import */
    IMPORT( "import"),
    
    /** Akce pro proces konvert */
    CONVERT( "convert"),
    
    /** Akce pro proces replicationrights */
    REPLICATIONRIGHTS("replicationrights"),
    
    /** Akce pro proces enumerator */
    ENUMERATOR( "enumerator"),

    /** Akce pro proces indexace */
    REINDEX( "reindex"),
    
    /** Akce pro proces replikace - periodika */
    REPLIKATOR_PERIODICALS( "replikator_periodicals"),
    
    /** Akce pro proces replikace - monografie */
    REPLIKATOR_MONOGRAPHS( "replikator_monographs"),
    
    /** Akce pro proces delete */
    DELETE( "delete"),
    
    /** Akce pro proces export FOXML*/
    EXPORT( "export"),
    
    /** Akce pro nastave priznaku PRIVATE, PUBLIC */
    SETPRIVATE( "setprivate"), SETPUBLIC( "setpublic"),
 
    ADMINISTRATE( "administrate"),

    /** Akce pro spusteni metadata editoru */
    EDITOR("editor"),
    
    /** Akce umozni spravu procesu */
    MANAGE_LR_PROCESS("manage_lr_process"),
    
    /** Akce pro replikace do K4 */
    EXPORT_K4_REPLICATIONS("export_k4_replications"),
    IMPORT_K4_REPLICATIONS("import_k4_replications"),
    
    EDIT_INFO_TEXT("edit_info_text"),
    
    /** Akce umozni spoustet editor uzivatelu superadmin modu */
    USERSADMIN("rightsadmin"),
    
    /** Akce umoznu spoustet editor v subadmin modu */
    USERSSUBADMIN("rightssubadmin"),

    /** Akce umoznuje spravu virtualnich sbirek */
    VIRTUALCOLLECTION_MANAGE("virtualcollection_manage"),
    
    /** Akce umoznuje sprav u kriterii */
    CRITERIUMS_RIGHTS_MANAGE("criteriums_rights_manage"),
    
    /** Akce umozni zobrazit administratorskou cast menu */
    DISPLAY_ADMIN_MENU("display_admin_menu");
    
    
    private String formalName;
  
    private SecuredActions(String formalName) {
        this.formalName = formalName;
    }

    public String getFormalName() {
        return formalName;
    }
    
    
    public static SecuredActions findByFormalName(String fname) {
        SecuredActions[] vals = values();
        for (SecuredActions act : vals) {
            if (act.getFormalName().equals(fname)) {
                return act;
            }
        }
        return null;
    }
}
