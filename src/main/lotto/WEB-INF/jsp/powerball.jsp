<!DOCTYPE HTML>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<html>
<head>
	<title>My Lotto</title>
	<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
</head>
<body>
	<form action="/powerball/uploadResult" method="post" enctype="multipart/form-data">
    <input type="file" name="file"/>&nbsp;<input type="submit"value="Upload New Result"/>
    </form>
    
    <form action="/powerball/ticket/new" method="post">
    <input type="text" name="draws" value="958"/>&nbsp;<input type="text" name="games" value="12"/>&nbsp;<input type="submit"value="New Ticket"/>
    </form>
    
    <h2>Draw Result ${result.drawNumber}</h2>
    <span>Number:</span>&nbsp;<span>${result.winningNumbers}, &nbsp;[${result.powerball}]</span>
    
    <h2>Ticket History</h2>
    <table>
    <c:if test="${not empty tickets}">
			<c:forEach var="ticket" items="${tickets}">
				<tr>
					<td>Ticket: <a href="/powerball/ticket/${ticket.id}">${ticket.draw}</a></td>
				</tr>
			</c:forEach>
		</c:if>
    
    </table>
    <script type="text/javascript">
    	
    </script>
</body>
</html>