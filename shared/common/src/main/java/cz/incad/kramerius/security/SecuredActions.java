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
 * Vyctovy typ chranenych akci systemu K7 
 * 
 */
public enum SecuredActions {

    /** Definice novych akci**/
    /** READ; ma pravo cist konkretni objekt */
    A_READ("a_read", false),
    
    /** READ; ma pravo vyuzit pdf resource */
    A_PDF_READ("a_pdf_read", false),
    
    /** smazani  zaznamu */
    A_DELETE("a_delete",false),
    
    /** spravovat vsechny procesy; smazat, prohlizet logy */
    A_PROCESS_EDIT("a_process_edit"),
    
    /** Pravo pro cteni procesu */
    A_PROCESS_READ("a_process_read"),
    
    
    /** Spravovat pouze vlastni procesy */
    A_OWNER_PROCESS_EDIT("a_owner_process_edit"),

    /** spusteni indexeru; cely repozitar*/
    A_INDEX("a_index", false),
    
    /** indexace ve stronu */
    //A_INDEX_CHILDREN("a_index_children"),

    /** process rebuild indexu */
    A_REBUILD_PROCESSING_INDEX("a_rebuild_processing_index", false),
    
    /** import */
    A_IMPORT("a_import"),

    /** pravo nastavovat priznak viditlnosti a licence */
    A_SET_ACCESSIBILITY("a_set_accessibility", false),

    
    /** export pro cdk */
    A_EXPORT_CDK("a_export_cdk"),
    
    /** zobrazeni statistik */
    A_STATISTICS("a_statistics"),
    
    /** moznost mazat statistiky */
    A_STATISTICS_EDIT("a_statistics_edit"),
    
    /** exportovani statistik tretim stranam */
    A_EXPORT_STATISTICS("a_export_statistics"),
    
    /** replikace - export  */
    A_EXPORT_REPLICATIONS("a_export_replications"),
    
    /** replikace - import */
    A_IMPORT_REPLICATIONS("a_import_replications"),

    /** editace prav, pro vsechny objekty krome sbirek*/
    A_RIGHTS_EDIT("a_rights_edit",false),

    /** Pravo cist criteria */
    A_CRITERIA_READ("a_criteria_read"),
    
    /** Cteni kolekci, pravo umoznujici cist informace z admin ponitu pro ceti    */
    A_COLLECTIONS_READ("a_collections_read"),
    
    /** editace kolekci, pridavani do kolekci atd..   */
    A_COLLECTIONS_EDIT("a_collections_edit", false),
    
    /** pravo byti zaraditelny do kolekce */
    A_ABLE_TOBE_PART_OF_COLLECTION("a_able_tobe_part_of_collections", false),
    
    
    /** spusteni nkp logu */
    A_GENERATE_NKPLOGS("a_generate_nkplogs"),
    
    /** editace roli */
    A_ROLES_EDIT("a_roles_edit"),
    
    A_ROLES_READ("a_roles_read"),

    /* Will be enabled in the future
    A_USERS_EDIT("a_users_edit"),
    A_USERS_READ("a_users_read"),
    */
    
    /** Pravo pro admin cteni admina */
    A_ADMIN_READ("a_admin_read"),

    /** SDNNT synchronization */
    A_SDNNT_SYNC("a_sdnnt_sync"),
    
    /** Object editation */
    A_OBJECT_EDIT("a_object_edit"),
    
    /** Admin openapi specification read */
    A_ADMIN_API_SPECIFICATION_READ("a_admin_api_specification_read");
    
    private String formalName;
    private boolean onlyGlobalAction;
    
    private SecuredActions(String formalName, boolean gA) {
        this.formalName = formalName;
        this.onlyGlobalAction = gA;
    }
    
    private SecuredActions(String formalName) {
        this.formalName = formalName;
        this.onlyGlobalAction = true;
    }
    
    public String getFormalName() {
        return formalName;
    }
    
    public boolean isGlobalAction() {
        return onlyGlobalAction;
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
