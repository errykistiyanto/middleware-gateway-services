package co.id.middleware.finnet.utils;

public class FinnetRCTextParser {

    public static String parse(String rc, String id) {
        String response = "";
        switch (rc.trim().toLowerCase()) {
            // General Finnet
            case "00":
                response = "Transaction succesful";
                break;
            case "14":
                response = "Number not found";
                break;
            case "68":
                response = "Transaction timeout";
                break;
            case "13":
                response = "Invalid Denom";
                break;
            case "96":
                response = "System Malfunction at H2H";
                break;
            case "31":
                response = "Bank not supported by switch";
                break;
            case "94":
                response = "Duplicate STAN";
                break;
            case "05":
                response = "Undefined error";
                break;
            case "70":
                response = "Voucher out of stock";
                break;
            case "79":
                response = "Phone number is blocked";
                break;
            case "81":
                response = "Phone number is expired";
                break;
            case "89":
                response = "Link to billing provider is down";
                break;
            case "78":
                response = "The transaction was rejected because there were two bills in the same month";
                break;
            case "77":
                response = "Transaction rejected because customer name is not the same in two bill with same phone number";
                break;
            case "88":
                response = "The transaction is rejected because the bill already paid in terminal or Other Bank";
                break;
            case "30":
                response = "Format Message error";
                break;
            case "90":
                response = "Time cut off";
                break;
            case "40":
                response = "Unknown transaction type";
                break;
            case "12":
//                response = "Not sign on";
                response = "Invalid transaction";
                break;
            case "80":
                response = "Bill reference not found";
                break;
            case "06":
                response = "An error occurred outside the known response code";
                break;
            case "92":
                response = "The transaction was rejected because the prefix number is not listed on H2H";
                break;
            case "91":
                response = "Issuer or switch is inoperative";
                break;

            //General screen case reversal
            //21 No action taken
            //22 Suspected malfunction
            case "22":
                response = "Sorry for a while transaction can not be made";
                break;

        }
        return response;
    }
}
