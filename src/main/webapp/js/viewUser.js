$(document).ready(function () {
    var changeDiscountFun = function () {
        $.ajax({
            type: "POST",
            url: '/AJAXController',
            data: {
                command: "change-discount",
                login: $('#login').val(),
                discount: $('#discount').val()
            },
            success: function (response) {
                if (!response['error']) {
                    console.log("change discount correct");
                } else {
                    console.log(response['error']);
                }

            },
            error: function (exception) {
                console.log(exception);
            }
        });
    };
    var viewComplaintsFun = function () {
        $('#complaints').empty();
        $.ajax({
            type: "POST",
            url: '/AJAXController',
            data: {
                command: "find-user-complaints",
                login: $('#login').val()
            },
            success: function (response) {
                if (!response['error']) {
                    response['complaints'].forEach(function (complaint) {
                        if(!complaint.accept){
                            $('#complaints').append("<div id='complaint-"+complaint.complaintId+"'>"+
                                "<div>"+complaint.complaintId+"</div>\n" +
                                "<div>"+complaint.raidId+"</div>\n" +
                                "<div>"+complaint.complaintText+"</div>\n"+
                                "<input type='submit' class='accept' id="+complaint.complaintId+" value='Ok'>\n"+
                                "</div>");
                            var acceptComplaintFun = function () {
                                removeComplaint=this.id;
                                $.ajax({
                                    type: "POST",
                                    url: '/AJAXController',
                                    data: {
                                        command: "accept-complaint",
                                        id: complaint.complaintId
                                    },
                                    success: function (response) {
                                        if (!response['error']) {
                                            console.log("accept complaint correct id:"+removeComplaint);
                                            $( "#"+complaint.complaintId).remove();
                                        } else {
                                            console.log(response['error']);
                                        }

                                    },
                                    error: function (exception) {
                                        console.log(exception);
                                    }
                                });
                            };
                            $('#'+complaint.complaintId).click(acceptComplaintFun);
                        }else {
                            $('#complaints').append("<div id='complaint-"+complaint.complaintId+"'>"+
                                "<div>"+complaint.complaintId+"</div>\n" +
                                "<div>"+complaint.raidId+"</div>\n" +
                                "<div>"+complaint.complaintText+"</div>\n"+
                                "</div>");
                        }
                    });
                } else {
                    console.log(response['error']);
                }

            },
            error: function (exception) {
                console.log(exception);
            }
        });
    };
    $('#view-complaints').click(viewComplaintsFun);
    $('#change-discount').click(changeDiscountFun);
});