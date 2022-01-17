<%-- 
    Document   : viewInvoiceAdmin
    Created on : 16 Jan 2022, 16:38:05
    Author     : ISA06002471
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<%
    request.setAttribute("selectedPage","viewInvoiceAdmin");
%>
<jsp:include page="header.jsp" />
<!-- Begin page content -->
<main role="main" class="container">
    <H1>All invoices</H1>
    
    <H1></H1>
    <table class="table">
        <thead>
            <tr>
                <th scope="col">Order ID</th>
                <th scope="col">Customer User Name</th>
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
                    <td>${invoice.purchaser.username}</td>
                    <td>${invoice.status}</td>
                    <td>${invoice.dateOfPurchase}</td>
                    <td>${invoice.amountDue}</td>
                    <td></td>
                    <td>
                        <!-- post avoids url encoded parameters -->
                        <form action="./viewModifyInvoice" method="GET">
                            <input type="hidden" name="invoiceNumber" value="${invoice.invoiceUUID}">
                            <button class="btn" type="submit" >View/Edit/Update</button>
                        </form> 
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</main>
