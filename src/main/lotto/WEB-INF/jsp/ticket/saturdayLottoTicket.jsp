<!DOCTYPE HTML>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page pageEncoding="UTF-8" %>

<html>
<head>
	<title>My Lotto</title>
	<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
</head>
<body>
    <a href="/saturdayLotto">Back</a>
    <h2>Ticket ${ticket.draw}</h2>
    
    <table>
			<c:forEach var="result" items="${ticket.results}">
				<tr>
					<td>${result.numbers}</td>
				</tr>
			</c:forEach>
	</table>
    
</body>
</html>