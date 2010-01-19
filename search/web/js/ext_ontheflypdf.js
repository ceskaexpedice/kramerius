// Check page range
function ext_ontheflypdf_checkPdfRange(from, to, maxRange, errorText){ 


if(isNaN(from) || isNaN(to))  {
    alert(errorText);
    return false;
}
from = parseInt(from);
to = parseInt(to);

if ((from > to) || (((to-from)+1) > maxRange) || (from < 1) || (to < 1)){
    alert(errorText);
    return false;
  } else {
      return true;
  }
}
