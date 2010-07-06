/**
 * Skupina je urcena pro generovani prvni stranky
 */
group firstpage;

/**
 * Generovani metadata na prvni strance
 * formalni parametr dc = Dublin core metadata zplostena do hashtable
 */ 
metadata(dc) ::= <<
		$dc.title$
		$dc.creator; separator=","$
>>


/**
 * Podminky pouziti
 */
description() ::= <<

 Podmínky využití
 
 Knihovna poskytuje přístup k digitalizovaným dokumentům pouze pro nekomerční, vědecké, studijní
 účely a pouze pro osobní potřeby uživatelů. Část dokumentů digitální knihovny podléhá autorským
 právům. Využitím digitální knihovny NK ČR a vygenerováním kopie části digitalizovaného
 dokumentu se uživatel zavazuje dodržovat tyto podmínky využití, které musí být součástí každé
 zhotovené kopie. Jakékoli další kopírování materiálu z digitální knihovny NK ČR není možné bez
 případného písemného svolení NK ČR.
 
 

>>