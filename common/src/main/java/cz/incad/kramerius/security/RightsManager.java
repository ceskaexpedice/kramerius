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

import java.sql.SQLException;
import java.util.List;

import cz.incad.kramerius.ObjectPidsPath;

/** 
 * DAO objekt pro spravu prav systemu K4
 * @author pavels
 */
public interface RightsManager {
    
    // najde prava pro uzivatele
    /**
     * Najde prava vztazena na objekty (definovane pidy), akci a daneho uzivatele
     * @param pids Objekty pro ktere hledame prava
     * @param action Chranena akce
     * @param user Uzivatel
     */
    public Right[] findRights(String[] pids, String action, User user);

    // interpretuje prava
    /**
     * Interpretace prav nad danym objektem
     * @param ctx Kontext pro interpretaci prav
     * @param pid PID objektu
     * @param path Cesta k objektu
     * @param action Chranena akce
     * @param user Uzivatel
     */
    public EvaluatingResult resolve(RightCriteriumContext ctx, String pid, ObjectPidsPath path, String action, User user) throws RightCriteriumException;
    
    // interpretuje prava skrz celou cestu.  Od listu az ke korenu.
    /**
     * Interpretace prav pro vsechny objekty v ceste (od listu az ke korenu)
     * @param ctx Kontext pro interpretaci prav
     * @param pid PID objektu
     * @param path Cesta k objektu
     * @param action Chranena akce
     * @param user Uzivatel
     */
    public EvaluatingResult[] resolveAllPath(RightCriteriumContext ctx, String pid, ObjectPidsPath path, String action, User user) throws RightCriteriumException;

    
    // najde prava pro skupinu
    /**
     * Najde vsechna prava definovana pro objekty, chranenou akci a roli
     * @param pids PIDy objektu
     * @param action Chranena akce
     * @param role Hledana role
     */
    public Right[] findRightsForGroup(final String[] pids, final String action, final Role role);

    // najde vsechna prava
    /**
     * Najde vsechna prava pro konkretni objekty a chranenou akci
     * @param pids PIDy objektu
     * @param action Chranena akce
     */
    public Right[] findAllRights(String[] pids, String action);

    
    // DAO methods - DAt to jinam !!
    // najde vsechny parametry 
    /**
     * Najde vsechny parametry
     */
    public RightCriteriumParams[] findAllParams();

    /**
     * Najde parametr pro dle zadaneho id
     * @param paramId Identifikator parametru
     * @return
     */
    public RightCriteriumParams findParamById(int paramId);

    /**
     * Find criterium by id 
     * @param critId
     * @return
     */
    public RightCriteriumWrapper findRightCriteriumById(int critId);
    
        
    public List<String> saturatePathAndCreatesPIDs(String uuid, String[] path);

    /**
     * Vlozit nove pravo
     * @param right Nove pravo
     * @return Vraci id nove zalozeneho prava
     * @throws SQLException
     */
    public int insertRight(Right right) throws SQLException;

    /**
     * Upravi existujici pravo
     * @param right Upravovane pravo
     * @throws SQLException
     */
    public void updateRight(Right right) throws SQLException;

    
    /**
     * Vytvori nove kriterium
     * @param criterium Nove vkladane kriterium
     * @return Vraci id nove vlozeneho kriteria
     * @throws SQLException
     */
    public int insertRightCriterium(RightCriteriumWrapper criterium) throws SQLException;

    /**
     * Upravi kriterium
     * @param criterium Upravovavane kriterium
     * @throws SQLException
     */
    public void updateRightCriterium(RightCriteriumWrapper criterium) throws SQLException;

    /**
     * Vlozi nove parametry
     * @param criteriumParams Nove vkladane parametry
     * @return Vraci id nove vlozenych parametru
     * @throws SQLException
     */
    public int insertRightCriteriumParams(RightCriteriumParams criteriumParams) throws SQLException;

    /**
     * Upravi parametry kriteria
     * @param criteriumParams Upravovane parametry
     * @throws SQLException
     */
    public void updateRightCriteriumParams(RightCriteriumParams criteriumParams) throws SQLException;

    /**
     * Smaze pravo 
     * @param right Mazane pravo
     * @throws SQLException
     */
    public void deleteRight(Right right) throws SQLException;
    
    
    /**
     * Najde pravo dle id
     * @param id Identifikator prava
     * @return
     */
    public Right findRightById(int id);
    
    
    /**
     * Najde vsechny pouzivane identifikatory roli
     * @return
     */
    public int[] findUsedRoleIDs();
}
