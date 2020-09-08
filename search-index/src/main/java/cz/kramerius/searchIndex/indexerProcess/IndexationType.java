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

}
