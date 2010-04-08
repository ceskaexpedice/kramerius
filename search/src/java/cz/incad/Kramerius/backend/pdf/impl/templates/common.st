group common;		


/**
 * Rendruje internal part
 */
render(title, model, bibliomods) ::=<<
$(model)(title=title, bibliomods = bibliomods)$
>>
