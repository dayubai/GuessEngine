<!DOCTYPE HTML>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<html>
<head>
	<title>My Lotto</title>
	<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
</head>
<body>
	<form action="/ozLotto/uploadResult" method="post" enctype="multipart/form-data">
    <input type="file" name="file"/>&nbsp;<input type="submit"value="Upload New Result"/>
    </form>
    
	<form action="/ozLotto/ticket/new" method="post">
    <input type="text" name="draws" value=""/>&nbsp;<input type="text" name="games" value="18"/>&nbsp;<input type="submit"value="New Ticket"/>
    </form>
    
    <c:if test="${not empty result}">
    <h2>Draw Result ${result.drawNumber}</h2>
    <span>Number:</span>&nbsp;<span>${result.winningNumbers}</span><br/><span>Supplementaries:</span><span>${result.supplementaryNumbers}</span>
    
    </c:if>
    
    <h2>Ticket History</h2>
    
    <table>
		<c:if test="${not empty tickets}">
			<c:forEach var="ticket" items="${tickets}">
				<tr>
					<td>Ticket: <a href="/ozLotto/ticket/${ticket.id}">${ticket.draw}</a></td>
				</tr>
			</c:forEach>
		</c:if>
	</table>
    
    <script type="text/javascript">
    	
    </script>
</body>
</html>