group internalpart;		

PERIODICAL(title,bibliomods) ::=<<
Titul: $title$ {font}(size=20)
{line}

Bibliomods metadata{font}(size=16)
Titul: $bibliomods.modsCollection.mods.titleInfo.content$
$if(bibliomods.modsCollection.mods.titleInfo.subTitle.content)$
Podtitul: $bibliomods.modsCollection.mods.titleInfo.subTitle.content$
$endif$
Urn: $bibliomods.modsCollection.mods.identifierurn$
Typ zdroje:  $bibliomods.modsCollection.mods.typeOfResource.content$
Model: PERIODICAL
>>

PERIODICALVOLUME(title,bibliomods) ::=<<
Titul: $title$ {font}(size=20)
{line}

Bibliomods metadata{font}(size=16)
Titul: $bibliomods.modsCollection.mods.titleInfo.content$
$if(bibliomods.modsCollection.mods.titleInfo.subTitle.content)$
Podtitul: $bibliomods.modsCollection.mods.titleInfo.subTitle.content$
$endif$
Urn: $bibliomods.modsCollection.mods.identifierurn$
Typ zdroje:  $bibliomods.modsCollection.mods.typeOfResource.content$
Číslo: $bibliomods.modsCollection.mods.part.detaitype.number$
Datum: $bibliomods.modsCollection.mods.part.date$
Model: PERIODICALVOLUME
>>



/**
 * Rendruje internal part
 */
INTERNALPART(title,bibliomods) ::=<<
Titul:$title$ {font}(size=20)
{line}
Bibliomods metadata{font}(size=16)

Titul: $bibliomods.modsCollection.mods.titleInfo.content$
$if(bibliomods.modsCollection.mods.titleInfo.subTitle.content)$
Podtitul: $bibliomods.modsCollection.mods.titleInfo.subTitle.content$
$endif$
Urn: $bibliomods.modsCollection.mods.identifierurn$
Typ zdroje:  $bibliomods.modsCollection.mods.typeOfResource.content$
Číslo: $bibliomods.modsCollection.mods.partPeriodicalIssue.detailissue.number$
Datum: $bibliomods.modsCollection.mods.partPeriodicalIssue.date$
Model: INTERNALPART
>>


/**
 * Rendruje periodcal item part
 */
PERIODICALITEM(title,bibliomods) ::=<<
Titul:$title$ {font}(size=20)
{line}

Bibliomods metadata{font}(size=16)
Titul:  $bibliomods.modsCollection.titleInfo.content$
$if(bibliomods.modsCollection.mods.titleInfo.subTitle.content)$
Podtitul: $bibliomods.modsCollection.mods.titleInfo.subTitle.content$
$endif$
Urn: $bibliomods.modsCollection.mods.identifierurn$
Typ zdroje:  $bibliomods.modsCollection.mods.typeOfResource.content$
Číslo: $bibliomods.modsCollection.mods.partPeriodicalIssue.detailissue.number$
Datum: $bibliomods.modsCollection.mods.partPeriodicalIssue.date$
Model: PERIODICALITEM
>>




MONOGRAPH(title,bibliomods) ::=<<
Titul:$title$ {font}(size=20)
{line}
Bibliomods metadata{font}(size=16)
$if(bibliomods.modsCollection.titleInfo.content)$
Titul:  $bibliomods.modsCollection.titleInfo.content$

$endif$
$if(bibliomods.modsCollection.mods.titleInfo.subTitle.content)$
Podtitul: $bibliomods.modsCollection.mods.titleInfo.subTitle.content$

$endif$
Urn: $bibliomods.modsCollection.mods.identifierurn$
Fyzicke ulozeni: $bibliomods.modsCollection.mods.location.physicalLocation$
$if(bibliomods.modsCollection.mods.originInfopublisher)$
$_publisher(publishermods=bibliomods.modsCollection.mods.originInfopublisher)$	

$endif$
$if(bibliomods.modsCollection.mods.originInfoprinter)$
$_printer(printermods=bibliomods.modsCollection.mods.originInfoprinter)$	

$endif$
$if(bibliomods.modsCollection.mods.namepersonal)$
$_creators(creatorsmods=bibliomods.modsCollection.mods.namepersonal)$	

$endif$
Typ zdroje:  $bibliomods.modsCollection.mods.typeOfResource.content$
Model: MONOGRAPH
>>

MONOGRAPHUNIT(title,bibliomods) ::=<<
Titul:$title$ {font}(size=20)
{line}
Bibliomods metadata{font}(size=16)
$if(bibliomods.modsCollection.titleInfo.content)$
Titul:  $bibliomods.modsCollection.titleInfo.content$

$endif$
$if(bibliomods.modsCollection.mods.titleInfo.subTitle.content)$
Podtitul: $bibliomods.modsCollection.mods.titleInfo.subTitle.content$

$endif$
Urn: $bibliomods.modsCollection.mods.identifierurn$
Fyzicke ulozeni: $bibliomods.modsCollection.mods.location.physicalLocation$
$if(bibliomods.modsCollection.mods.originInfopublisher)$
$_publisher(publishermods=bibliomods.modsCollection.mods.originInfopublisher)$	

$endif$
$if(bibliomods.modsCollection.mods.originInfoprinter)$
$_printer(printermods=bibliomods.modsCollection.mods.originInfoprinter)$	
$endif$

Model: MONOGRAPHUNIT
>>

_creators(creatorsmods)  ::=<<
Autori:$creatorsmods:{autor| $autor.namePartfamily$ $autor.namePartgiven$}; separator=","$
>>


_publisher(publishermods) ::=<<
Vydal:$publishermods.publisher.content$
Misto: $publishermods.place.placeTermtext.content$
>>

_printer(printermods) ::=<<
Vytiskl:$printermods.publisher.content$
Misto: $printermods.place.placeTerm.content$
>>

_printer(printermods) ::=<<
Vytiskl:$printermods.publisher.content$
Misto: $printermods.place.placeTerm.content$
>>





