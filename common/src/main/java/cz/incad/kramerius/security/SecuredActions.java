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
 * Secured actions enum 
 */
public enum SecuredActions {

    // common user actions
    READ("read"),
    
    // Admin actions
    IMPORT( "import"),
    CONVERT( "convert"),
    REPLICATIONRIGHTS( "replicationrights"),
    ENUMERATOR( "enumerator"),
    REINDEX( "reindex"),
    REPLIKATOR_PERIODICALS( "replikator_periodicals"),
    REPLIKATOR_MONOGRAPHS( "replikator_monographs"),
    DELETE( "delete"),
    EXPORT( "export"),
    SETPRIVATE( "setprivate"),
    SETPUBLIC( "setpublic"),
    ADMINISTRATE( "administrate"),
    EDITOR("editor"),
    MANAGE_LR_PROCESS("manage_lr_process"),
    EXPORT_K4_REPLICATIONS("export_k4_replications"),
    IMPORT_K4_REPLICATIONS("import_k4_replications"),
    
    // editace informaci na uvodni strance
    EDIT_INFO_TEXT("edit_info_text"),
    
    // editace uzivatelu
    USERSADMIN("rightsadmin"),
    USERSSUBADMIN("rightssubadmin"),

    // sprava virtualnich sbirek !
    VIRTUALCOLLECTION_MANAGE("virtualcollection_manage"),
    
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
