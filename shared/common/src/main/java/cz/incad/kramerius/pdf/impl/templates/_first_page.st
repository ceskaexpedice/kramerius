<commands>
    <paragraph spacing-after="3" spacing-before="20">
        <textsarray>
        <text font-formal-name="logo">Kramerius 5</text>
        </textsarray>
    </paragraph>

    <paragraph  spacing-after="3">
        <textsarray>
        <text font-formal-name="normal">$viewinfo.ditigalLibrary$</text>
        </textsarray>
    </paragraph>
        
    <break></break>

    <paragraph  spacing-after="10">
        <textsarray>
        <text font-formal-name="normal">  </text>
     </textsarray>
</paragraph>

    
    <paragraph  spacing-after="3">
        <textsarray>
        <text font-formal-name="normal">$viewinfo.conditionUsage$  </text>
     </textsarray>
</paragraph>

<paragraph  spacing-after="20">
<textsarray>
<text font-formal-name="normal" hyph-country="$viewinfo.hyphCountry$" hyph-lang="$viewinfo.hyphLang$">
$viewinfo.conditionUsageText$
</text>
</textsarray>
</paragraph>


$if(viewinfo.moreGeneratedItems)$ 

<list list-type="unordered">
$viewinfo.generatedItems:{generatedItem| 

    $if(generatedItem.detailItemsDefined)$
        $generatedItem.detailItems:{detail |
            <item list-symbol=" ">
                <textsarray>
                    <text font-formal-name="normal">$detail.key$: </text>
                    <text font-formal-name="strong">$detail.value$</text>
                </textsarray>
            </item>
        }$
    $endif$
    
}$     
</list>
$endif$ 
</commands>