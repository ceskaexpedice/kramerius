package cz.kramerius.searchIndex.indexerProcess;

public enum IndexationType {

    /**
     * Indexuje se jen samotný objekt (dříve fromPid).
     * Vhodné po úpravě záznamu, která neovlivní indexy potomků. Např. nakladatelské údaje monografie.
     * Nevhodné po úpravě, která ovlivní indexy potomků (název se přepíše do polí root.title, own_parent.title potomků; pořadí stránek/vydání se projeví v poli rels_ext_index.sort)
     */
    OBJECT,

    /**
     * Indexuje se samotný objekt a jeho přímí potomci (vlastní i nevlastní).
     * Vhodné po úpravě záznamu objektu, který mohl ovlivnit indexy přímých potomků. Např. změna pořadí stránek/vydání, která se projeví v poli rels_ext_index.sort
     * Nevhodné po úpravě, která ovlivní indexy nepřímých potomků, jako je změna názvu (přepíše se do root.title všech vlastních potomků)
     */
    OBJECT_AND_CHILDREN,

    /**
     * Indexuje se celý strom s tímto objektem jako kořenem.
     * Nevlastní potomci a jejich stromy se tedy vynechávají. Např. zaindexuje se článek v ročníku periodika, ale už ne stránky, na které článek ukazuje přes isOnPage.
     */
    TREE,

    /**
     * Prochází se celý strom s tímto objektem jak kořenem, indexují se ale jen novejší záznamy.
     * Novější záznamy jsou ty, u kterých je datum v repozitáři novější než datum v indexu.
     */
    TREE_INDEX_ONLY_NEWER, //TODO: not properly implemented yet, works as if everything was newer

    /**
     * Prochází se strom s tímto objektem jak kořenem, ale jen ty záznamy, které jsou novější, všechny takové záznamy se indexují (dříve fromKrameriusModel).
     * Tedy záznamy, jejichž datum v repozitáři není novější, než datum v indexu, se přeskočí a jejich strom se nezpracovává.
     */
    TREE_PROCESS_ONLY_NEWER, //TODO: not properly implemented yet, works as if everything was newer

    /**
     * Indexují se jen stránky, které jsou součástí stromu. Zbytek stromu se prochází, ale neinxexuje.
     * Určeno pro změny v OCR datech.
     */
    TREE_INDEX_ONLY_PAGES, //TODO: not properly implemented yet, works as if nothing was page

    /**
     * Indexují se všechny objekty, které jsou součástí stromu, kromě stránek.
     * Určeno pro změny ve struktuře bez nutnosti reindexace OCR dat.
     * Pozor na změny v názvu rootu a pozici stránek, případně další informace, které by se měly propsat do indexů stránek.
     */
    TREE_INDEX_ONLY_NONPAGES, //TODO: not properly implemented yet, works as if nothing was page

    /*
     Indexuje se celý strom a také stromy všech nevlastních potomků (dříve reindexDoc).
     Např. zaindexuje se jak článek, tak stránky v článku (odkazované přes isOnPage). Anebo sbírka, včetně stromů všech objektů ve sbírce a jejích podsbírkách.
     */
    TREE_AND_FOSTER_TREES,

    //COLLECTION_ITEMS,
    //TODO: optimalizace: nový typ indexace COLLECTION_ITEMS, co by řešil jen věci související se sbírkami a licencemi
    //Musí pracovat korektně s licencemi a udržovat si např. seznam licencí předků (nejen vlastních), přepisovat dolů i licence, jako to dela Indexer
    //Také musí aktualizovat timestampy (pole indexed) kvuli sklizeni zmen, ale nemenit hodnotu v poli indexer_version
    //uplatneni:
    //1. pridani objektu do sbirky
    //2. odebrani objektu ze sbirky
    //3. odebrani licence sbirce (pole in_collections.*: nestaci jen atomic updaty odebrat hodnoty vsem objektum primo ve sbirce, ty totiz muzou patrit do sbirky dale neprimo pres jinou sbirku)
    //Nestaci zde jen atomic updaty aktualizovat hodnoty z vybranych poli pro cele podstromy objektu ve sbirce (in_collections, in_collections.direct,licenses_of_ancestors).
    //Protoze:
    //pid_paths             - je potreba aktualizovat pid_paths a vypocitat pro kazdy jeden uzel na zaklade toho, vsech jejich (i nevlastnich) predku a jejich hodnot pid_paths, coz se zmeni pri strukturalni zmene (pridani do/odebrani ze sbirky)
    //in_collections        - i pri odstraneni objektu ze sbirky objekt muze do sbirky stale patrit a to neprimo pres jinou sbirku: S1 -> X rusim, ale S1 -> S2 -> X zustava, proto X musi mit nadale v poli in_collections hodnotu S1
    //licenses_of_ancestors - pri odebrani licence L sbirce S1, ktera obsahuje X, anebo pri odstraneni licence L ze sbirky S1 nestaci jen atomic updaty odebrat celemu (pod)stromu X hodnotu L z licenses_of_ancestors,
    //                        X muze mit stale na licenci narok a to z jineho zdroje (jeho vlastni predek, jina sbirka)
    //indexed               - kvuli sklizeni zmen je potreba aktualizovat
    //Obecna indexace tohle vsechno resi, nicmene dela i veci, ktere v techto scenanarich nejsou relevantni (typicky indexace fulltextu).

}
