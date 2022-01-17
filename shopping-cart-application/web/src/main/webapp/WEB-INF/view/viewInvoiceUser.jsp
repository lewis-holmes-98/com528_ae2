<%-- 
    Document   : viewInvoiceUser
    Created on : 16 Jan 2022, 16:37:54
    Author     : ISA06002471
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    request.setAttribute("selectedPage","viewInvoiceUser");
%>
<jsp:include page="header.jsp" />
<!-- Begin page content -->
<main role="main" class="container">
    <H1>Your Orders</H1>
    
    <H1></H1>
    <table class="table">
        <thead>
            <tr>
                <th scope="col">Order ID</th>
                <th scope="col">Status</th>
                <th scope="col">Date</th>
                <th scope="col">Cost</th>
                
                <th></th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="invoice" items="${invoices}">
                <tr>
                    <td>${invoice.uuid}</td>
                    <td>${invoice.status}</td>
                    <td>${invoice.dateOfPurchase}</td>
                    <td>${invoice.amountDue}</td>
                    <td></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</main>
