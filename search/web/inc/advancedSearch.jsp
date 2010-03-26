<%@page contentType="text/html" pageEncoding="UTF-8"%>
<div id="advSearch" class="advSearch">
    <img src="img/x.png" align="right" onclick="$('#advSearch').hide();"/><br/>
    <table class="advancedSearch">
        <col width="150px">
        <tbody>
            <tr><td colspan="2"><strong>Metadata<strong></strong></strong></td></tr>
            <tr>
                <td>ISSN/ISBN</td>
                <td><input type="text" value="<c:out value="${param.issn}" />" size="20" name="issn"></td>
            </tr>
            <tr>
                <td>Název titulu</td>
                <td><input type="text" value="<c:out value="${param.title}" />" size="20" name="title"></td>
            </tr>
            <tr>
                <td>Autor</td>
                <td><input type="text" value="<c:out value="${param.author}" />" size="20" name="author"></td>
            </tr>
            <tr>
                <td>Rok</td>
                <td><input type="text" value="<c:out value="${param.rok}" />" size="10" name="rok"></td>
            </tr>
            <tr>
                <td>MDT</td>
                <td><input type="text" value="<c:out value="${param.udc}" />" size="20" name="udc"></td>
            </tr>
            <tr>
                <td>DDT</td>
                <td><input type="text" value="<c:out value="${param.ddc}" />" size="20" name="ddc"></td>
            </tr>
            <tr>
                <td>Klíčová slova</td>
                <td><input type="text" value="<c:out value="${param.keywords}" />" size="20" name="keywords"></td>
            </tr>
            <tr>
                <td>Pouze veřejné dokumenty</td>
                <td><input type="checkbox" value="on" name="onlyPublic"></td>
            </tr>
    </tbody></table>
</div>