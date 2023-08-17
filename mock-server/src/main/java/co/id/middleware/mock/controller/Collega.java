package co.id.middleware.mock.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author errykistiyanto@gmail.com 2020-10-19
 */

@RestController
@Component("CollegaServices")
@Slf4j
public class Collega {

    //logstash message direction
    public static final String apps = "mock-server";
    public static final String req_from = "request from ";
    public static final String req_to = "request to ";
    public static final String resp_from = "response from ";
    public static final String resp_to = "response to ";
    public static final String host = "collega";

    @Autowired
    private Environment env;

    @RequestMapping(value = "/Gateway/gateway/services/v2/postData", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.POST)
    public ResponseEntity<String> Services(@RequestBody Map<String, Object> cgson) throws JsonProcessingException {

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(gson.toJson(cgson));
        String reqId = root.path("reqId").asText();

        String service = "";
        String response = "";
        switch (reqId) {
            case "00002":
                service = "1.1 Account and Balance Inquiry (reqId = 00002)";
                response = "{\n" +
                        "   \"result\": {\n" +
                        "      \"FIRSTDEP\": 500000,\n" +
                        "      \"SALDO_BLOKIR\": 0,\n" +
                        "      \"IDNBR\": \"3301142505610002\",\n" +
                        "      \"SALDO_EFEKTIF\": -100261115.84,\n" +
                        "      \"MINWTHDR\": 0,\n" +
                        "      \"MAXWTHDRDAY\": 10000000000000000,\n" +
                        "      \"CIFTYPE\": 0,\n" +
                        "      \"CCYID\": \"IDR\",\n" +
                        "      \"PRODID\": \"02\",\n" +
                        "      \"BRANCHID\": \"5340\",\n" +
                        "      \"FLGAFI\": 0,\n" +
                        "      \"SSALDO_EFEKTIF\": \"-100,261,115.84\",\n" +
                        "      \"FLGDRAWMIN\": 0,\n" +
                        "      \"ACCNBR\": \"53400102000011\",\n" +
                        "      \"ACCSTS\": 1,\n" +
                        "      \"NOHP\": \"0827262172182\",\n" +
                        "      \"SSALDO_KLIRING\": \"0.00\",\n" +
                        "      \"SMUTCR\": \"0.00\",\n" +
                        "      \"DTBAL_BEF\": \"2021-06-02\",\n" +
                        "      \"SMUTDB\": \"0.00\",\n" +
                        "      \"APPLID\": \"01\",\n" +
                        "      \"SBODBAL\": \"-100,361,115.84\",\n" +
                        "      \"DTBAL\": \"2021-08-02\",\n" +
                        "      \"ACCTYPE\": 0,\n" +
                        "      \"SSALDO_AKHIR\": \"-100,361,115.84\",\n" +
                        "      \"SALDO_KLIRING\": 0,\n" +
                        "      \"NPWP\": \"695296582522000\",\n" +
                        "      \"FLGJOIN\": 0,\n" +
                        "      \"TXSTS\": 2,\n" +
                        "      \"FULLNM\": \"RUBANGI\",\n" +
                        "      \"MUTDB\": 0,\n" +
                        "      \"SSALDO_BLOKIR\": \"0.00\",\n" +
                        "      \"APPLID_BE\": \"01\",\n" +
                        "      \"NICKNM\": \"RUBANGI\",\n" +
                        "      \"MINSVG\": 10000,\n" +
                        "      \"BRTDT\": \"1961-05-25\",\n" +
                        "      \"SALDO_AKHIR\": -100361115.84,\n" +
                        "      \"MAXWTHDR\": 10000000000000000,\n" +
                        "      \"BODBAL_BEF\": -100233326.04,\n" +
                        "      \"FLGSWEEP\": 0,\n" +
                        "      \"ACCESLVL\": 1,\n" +
                        "      \"SBLOKIR_AFILIASI\": \"0.00\",\n" +
                        "      \"CIFID\": \"0000000357\",\n" +
                        "      \"MOTHRNM\": \"TOYIBAH\",\n" +
                        "      \"BODBAL\": -100361115.84,\n" +
                        "      \"ADDR\": \"JALAN H MUSTOFA 28\",\n" +
                        "      \"BLOKIR_AFILIASI\": 0,\n" +
                        "      \"MINBAL\": 100000,\n" +
                        "      \"MUTCR\": 0\n" +
                        "   },\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"rCode\": \"00\",\n" +
                        "   \"message\": \"INQUIRY REKENING BERHASIL\"\n" +
                        "}";
                break;

            case "00008":
                service = "1.2 Account Statements (reqId=00008)";
                response = "{\n" +
                        "   \"message\": \"INQUIRY MUTASI TRANSAKSI BERHASIL\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"result\": [\n" +
                        "      {\n" +
                        "         \"TXCODE\": \"296\",\n" +
                        "         \"TXDATE\": \"2019-09-01\",\n" +
                        "         \"TXAMT\": \"300000.00\",\n" +
                        "         \"TXDTSTLMN\": \"2019-09-01 14:28:40.496536\",\n" +
                        "         \"ACCNBR\": \"00102110048825\",\n" +
                        "         \"TXID\": \"k013400010\",\n" +
                        "         \"DBCR\": 1,\n" +
                        "         \"TXMSG\": \"test\",\n" +
                        "         \"TXBRANCH\": \"000\",\n" +
                        "         \"TXCCY\": \"IDR\",\n" +
                        "         \"USERID\": \"k0134\",\n" +
                        "         \"CHANNELID\": 1,\n" +
                        "         \"TXREVSTS\": 0,\n" +
                        "         \"GTWDATE\": \"\"\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"rCode\": \"00\",\n" +
                        "   \"saldoAwal\": \"-2007390.00\",\n" +
                        "   \"saldoAkhir\": \"-2507390.00\"\n" +
                        "}";
                break;

            case "00064":
                service = "1.3 Account Status Inquiry (reqId=00064)";
                response = "{\n" +
                        "   \"message\": \"INQUIRY REKENING BERHASIL\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"rCode\": \"00\",\n" +
                        "   \"accFound\": 1,\n" +
                        "   \"applId\": \"01\",\n" +
                        "   \"accNbr\": \"0010107165666\",\n" +
                        "   \"accName\": \"PT. COLLEGA INTI PRATAMA\",\n" +
                        "   \"accStatus\": 1,\n" +
                        "   \"branchId\": \"001\"\n" +
                        "}";
                break;

            case "00029":
                service = "1.4 Last Account Statements (reqId=00029)";
                response = "{\n" +
                        "   \"message\": \"INQUIRY MUTASI TRANSAKSI BERHASIL\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"result\": [\n" +
                        "      {\n" +
                        "         \"TXCODE\": \"201\",\n" +
                        "         \"TXDATE\": \"2017-08-01\",\n" +
                        "         \"TXAMT\": \"200000.00\",\n" +
                        "         \"TXDTSTLMN\": \"2017-08-01 09:05:10.65342\",\n" +
                        "         \"ACCNBR\": \"00102110048825\",\n" +
                        "         \"TXID\": \"k013400010\",\n" +
                        "         \"DBCR\": 1,\n" +
                        "         \"TXMSG\": \"SETORAN TUNAI\",\n" +
                        "         \"TXBRANCH\": \"001\",\n" +
                        "         \"TXCCY\": \"IDR\",\n" +
                        "         \"USERID\": \"k0134\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"TXCODE\": \"196\",\n" +
                        "         \"TXDATE\": \"2015-08-01\",\n" +
                        "         \"TXAMT\": \"100000.00\",\n" +
                        "         \"TXDTSTLMN\": \"2017-08-01 12:55:18.45645\",\n" +
                        "         \"ACCNBR\": \"00102110048825\",\n" +
                        "         \"TXID\": \"k013400011\",\n" +
                        "         \"DBCR\": 0,\n" +
                        "         \"TXMSG\": \"TRF-TEST\",\n" +
                        "         \"TXBRANCH\": \"001\",\n" +
                        "         \"TXCCY\": \"IDR\",\n" +
                        "         \"USERID\": \"k0134\"\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"rCode\": \"00\",\n" +
                        "   \"saldoAwal\": \"-5200000.00\",\n" +
                        "   \"saldoAkhir\": \"-5300000.00\"\n" +
                        "}";
                break;

            case "00005":
                service = "1.5 Transaction (reqId=00005)";
                response = "{\n" +
                        "   \"message\": \"TRANSAKSI BERHASIL\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"result\": {\n" +
                        "      \"TXID\": \"k022900006\"\n" +
                        "   },\n" +
                        "   \"rCode\": \"00\"\n" +
                        "}";
                break;

            case "00077":
                service = "1.6 Transaction Inquiry (reqId=00077)";
                response = "{\n" +
                        "   \"message\": \"INQUIRY TRANSAKSI BERHASIL\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"result\": [\n" +
                        "      {\n" +
                        "         \"TXCODE\": \"199\",\n" +
                        "         \"TXDATE\": \"2020-03-01\",\n" +
                        "         \"ACCNBR\": \"1400201000256\",\n" +
                        "         \"TXDTSTLMN\": \"2020-03-01 15:03:08.080848\",\n" +
                        "         \"DBCR\": 0,\n" +
                        "         \"TXID\": \"i213500001\",\n" +
                        "         \"TXMSG\": \"PAY_CMS_6_1.01.1.1_4.1.1.01.14\",\n" +
                        "         \"TXBRANCH\": \"140\",\n" +
                        "         \"CHANNELID\": 5,\n" +
                        "         \"TXAMT\": 1000,\n" +
                        "         \"TXCCY\": \"IDR\",\n" +
                        "         \"USERID\": \"i2135\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"TXCODE\": \"299\",\n" +
                        "         \"TXDATE\": \"2020-03-01\",\n" +
                        "         \"ACCNBR\": \"100001110300403360\",\n" +
                        "         \"TXDTSTLMN\": \"2020-03-01 15:03:08.080848\",\n" +
                        "         \"DBCR\": 1,\n" +
                        "         \"TXID\": \"i213500001\",\n" +
                        "         \"TXMSG\": \"PAY_CMS_6_1.01.1.1_4.1.1.01.14\",\n" +
                        "         \"TXBRANCH\": \"140\",\n" +
                        "         \"CHANNELID\": 5,\n" +
                        "         \"TXAMT\": 1000,\n" +
                        "         \"TXCCY\": \"IDR\",\n" +
                        "         \"USERID\": \"i2135\"\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"rCode\": \"00\"\n" +
                        "}";
                break;

            case "00031":
                service = "2.1 CIF Inquiry by Account (reqId = 00031)";
                response = "{\n" +
                        "   \"result\": {\n" +
                        "      \"IDNBR\": \"3301211203610002\",\n" +
                        "      \"PREDEGREE\": \"\",\n" +
                        "      \"FLGCITIZEN\": 1,\n" +
                        "      \"PROVNMJOB\": \"Jawa Tengah\",\n" +
                        "      \"AMTBAL\": 0,\n" +
                        "      \"CITYNMJOB\": \"Kabupaten Cilacap\",\n" +
                        "      \"EMAIL\": \"\",\n" +
                        "      \"OPENDT\": \"2020-01-02 00:00:00.0\",\n" +
                        "      \"CIFTYPE\": 0,\n" +
                        "      \"CPLNAME\": \"DINI\",\n" +
                        "      \"NOTELPJOB\": \"\",\n" +
                        "      \"CCYID\": \"IDR\",\n" +
                        "      \"LASTTXDT\": \"2020-05-04\",\n" +
                        "      \"PRODID\": \"03\",\n" +
                        "      \"COUNTRYIDJOB\": \"ID\",\n" +
                        "      \"BRANCHID\": \"5340\",\n" +
                        "      \"cifAddr\": [],\n" +
                        "      \"COUNTRYID\": \"ID\",\n" +
                        "      \"FLGBU\": 2,\n" +
                        "      \"STSPRIORITY\": \"\",\n" +
                        "      \"ACCSTS\": 1,\n" +
                        "      \"DHNSTS\": 1,\n" +
                        "      \"KD_LOKASI\": \"7 \",\n" +
                        "      \"NOHP\": \"0812912123121\",\n" +
                        "      \"PROVIDJOB\": \"33\",\n" +
                        "      \"STSADDR\": \"\",\n" +
                        "      \"INSURED\": 2,\n" +
                        "      \"KELNM\": \"3301020001\",\n" +
                        "      \"APPLID\": \"02\",\n" +
                        "      \"DTSTARTJOB\": \"1989-01-02\",\n" +
                        "      \"FUNCJOB\": \"008\",\n" +
                        "      \"TXADD\": \"1\",\n" +
                        "      \"TYPEID\": \"1 \",\n" +
                        "      \"ACCOUNTSTATUS\": \"AKTIF\",\n" +
                        "      \"EXPDT\": \"9999-12-31\",\n" +
                        "      \"CITYIDJOB\": \"3301\",\n" +
                        "      \"BUSSID\": \"01\",\n" +
                        "      \"POFUND\": \"SIMPANAN,-,-,-,-\",\n" +
                        "      \"FUNCDESC\": \"\",\n" +
                        "      \"LASTEDUID\": \"04\",\n" +
                        "      \"NPWP\": \"212312123243211\",\n" +
                        "      \"STSPEP\": 0,\n" +
                        "      \"RT\": \"003\",\n" +
                        "      \"PROVNM\": \"Jawa Tengah\",\n" +
                        "      \"RW\": \"004\",\n" +
                        "      \"RELIGIONID\": \"1\",\n" +
                        "      \"CITYID\": \"3301\",\n" +
                        "      \"SEX\": 1,\n" +
                        "      \"PARM_NEGARA\": \"\",\n" +
                        "      \"FULLNM\": \"TRI BUDIONO\",\n" +
                        "      \"BRTPLACE\": \"CILACAP\",\n" +
                        "      \"POSTALCODEJOB\": \"32112\",\n" +
                        "      \"JOBID\": \"002\",\n" +
                        "      \"STSNATION\": 1,\n" +
                        "      \"BRANCHOC\": \"\",\n" +
                        "      \"STSRISK\": 1,\n" +
                        "      \"FULLNMFAM\": \"\",\n" +
                        "      \"TAXID\": 2,\n" +
                        "      \"NOFAXJOB\": \"\",\n" +
                        "      \"cifHeirs\": [],\n" +
                        "      \"BRTDT\": \"1961-03-06\",\n" +
                        "      \"NICKNM\": \"TRI BUDIONO\",\n" +
                        "      \"HOMEID\": \"0\",\n" +
                        "      \"POSTALCODE\": \"32112\",\n" +
                        "      \"SURENM\": \"TRI BUDIONO\",\n" +
                        "      \"FATHRNM\": \"\",\n" +
                        "      \"PROVID\": \"33\",\n" +
                        "      \"COMID\": \"\",\n" +
                        "      \"KECNM\": \"3301020\",\n" +
                        "      \"CIFID\": \"0000000388\",\n" +
                        "      \"CITYNM\": \"Kabupaten Cilacap\",\n" +
                        "      \"MOTHRNM\": \"RETNO\",\n" +
                        "      \"NOTELP\": \"\",\n" +
                        "      \"NOFAX\": \"\",\n" +
                        "      \"TXMAIN\": \"2\",\n" +
                        "      \"ADDR\": \"JALAN MADEYASA\",\n" +
                        "      \"ADDRJOB\": \"JALAN ANGSANA\",\n" +
                        "      \"COMNMJOB\": \"DINAS PERTANIAN PROPINSI\",\n" +
                        "      \"MAINSALID\": \"GAJI\",\n" +
                        "      \"NIP\": \"1212312121234\",\n" +
                        "      \"MARRIAGEID\": \"1\",\n" +
                        "      \"BIRTHDT\": \"1961-03-06\",\n" +
                        "      \"POSTDEGREE\": \"\",\n" +
                        "      \"TXCASH\": 7000000,\n" +
                        "      \"PRODNM\": \"Tabungan Simantap Pensiun iB (Wadiah)\"\n" +
                        "   },\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"rCode\": \"00\",\n" +
                        "   \"message\": \"Inquiry Nasabah Berhasil\"\n" +
                        "}";
                break;

            case "00126":
                service = "3.1 Opening a Deposit Account (reqId=00126)";
                response = "{\n" +
                        "   \"message\": \"CREATE REKENING DEPOSITO BERHASIL\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"result\": {\n" +
                        "      \"CIFID\": \"0000854284\",\n" +
                        "      \"ACCNBR\": \"5100401002480\"\n" +
                        "   },\n" +
                        "   \"rCode\": \"00\"\n" +
                        "}";
                break;

            case "00127":
                service = "3.2 Placement of Deposit Funds (reqId=00127)";
                response = "{\n" +
                        "   \"message\": \"PENEMPATAN DEPOSITO BERHASIL\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"result\": {\n" +
                        "      \"BILYTNBR\": \"12345\",\n" +
                        "      \"SERIBLYTNBR\": \"XX\",\n" +
                        "      \"TXID\": \"s999900005\"\n" +
                        "   },\n" +
                        "   \"rCode\": \"00\"\n" +
                        "}";
                break;

            case "00135":
                service = "3.3 Change or Extend Time Deposit with ARO or Non ARO (reqId=00135)";
                response = "{\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"rCode\": \"00\",\n" +
                        "   \"message\": \"PERUBAHAN STATUS ARO/NON-ARO DEPOSITO BERHASIL\"\n" +
                        "}";
                break;

            case "00128":
                service = "3.4 Close Deposit Account (reqId=00128)";
                response = "{\n" +
                        "   \"message\": \"PENUTUPAN DEPOSITO BERHASIL\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"result\": {\n" +
                        "      \"ACCNBR\": \"0010404001473\",\n" +
                        "      \"DISBACC\": \"5040100001214\",\n" +
                        "      \"TXID\": \"0404001473\",\n" +
                        "      \"CIFID\": \"0000164837\"\n" +
                        "   },\n" +
                        "   \"rCode\": \"00\"\n" +
                        "}";
                break;

            case "00019":
                service = "4.1 Banking Product Inquiry (reqId = 00019)";
                response = "{\n" +
                        "   \"result\": [\n" +
                        "      {\n" +
                        "         \"APPLID\": \"01\",\n" +
                        "         \"PRODID\": \"01\",\n" +
                        "         \"PRODNM\": \"Product Giro 01\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"APPLID\": \"01\",\n" +
                        "         \"PRODID\": \"02\",\n" +
                        "         \"PRODNM\": \"Product Giro 02\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"APPLID\": \"03\",\n" +
                        "         \"PRODID\": \"01\",\n" +
                        "         \"PRODNM\": \"Product Deposito 01\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"APPLID\": \"03\",\n" +
                        "         \"PRODID\": \"02\",\n" +
                        "         \"PRODNM\": \"Product Deposito 02\"\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"rCode\": \"00\",\n" +
                        "   \"message\": \"INQUIRY PRODUCT BERHASIL\"\n" +
                        "}";
                break;

            case "00003":
                service = "4.2 Branch Inquiry (reqId = 00003)";
                response = "{\n" +
                        "   \"result\": [\n" +
                        "      {\n" +
                        "         \"BRANCHNM\": \"KANTOR PUSAT JAKARTA\",\n" +
                        "         \"CITY\": \"JAKARTA\",\n" +
                        "         \"ADDRESS\": \"JL. CIKINI RAYA\",\n" +
                        "         \"BRANCHID\": \"5000\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"BRANCHNM\": \"KCU BANDA ACEH\",\n" +
                        "         \"CITY\": \"BANDA ACEH\",\n" +
                        "         \"ADDRESS\": \"BANDA ACEH\",\n" +
                        "         \"BRANCHID\": \"5340\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"BRANCHNM\": \"KCP LHOKSEUMAWE\",\n" +
                        "         \"CITY\": \"LHOKSEUMAWE\",\n" +
                        "         \"ADDRESS\": \"LHOKSEUMAWE\",\n" +
                        "         \"BRANCHID\": \"5341\"\n" +
                        "      },\n" +
                        "      {\n" +
                        "         \"BRANCHNM\": \"KK SIGLI\",\n" +
                        "         \"CITY\": \"SIGLI\",\n" +
                        "         \"ADDRESS\": \"SIGLI\",\n" +
                        "         \"BRANCHID\": \"5342\"\n" +
                        "      }\n" +
                        "   ],\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"rCode\": \"00\",\n" +
                        "   \"message\": \"INQUIRY CABANG BERHASIL\"\n" +
                        "}";
                break;

            case "00037":
                service = "5.1 Opening a Saving Account (reqId=00037)";
                response = "{\n" +
                        "   \"message\": \"CREATE REKENING BERHASIL\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"result\": {\n" +
                        "      \"CIFID\": \"0000225669\",\n" +
                        "      \"ACCNBR\": \"1000202110790\"\n" +
                        "   },\n" +
                        "   \"rCode\": \"00\"\n" +
                        "}";
                break;

            case "000191": // sample doc spec 00019
                service = "5.2 Saving Account Activation (reqId=00019)";
                response = "{\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"rCode\": \"00\",\n" +
                        "   \"message\": \"Aktivasi Rekening Berhasil\"\n" +
                        "}";
                break;

            case "00106": // sample doc spec 00128
                service = "5.3 Close Saving Account (reqId=00106)";
                response = "{\n" +
                        "   \"message\": \"PENUTUPAN TABUNGAN BERHASIL\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"result\": {\n" +
                        "      \"ACCNBR\": \"01002036210004\",\n" +
                        "      \"DISBACC\": \"01002036210004\",\n" +
                        "      \"TXID\": \"0202001473\",\n" +
                        "      \"CIFID\": \"0000797562\"\n" +
                        "   },\n" +
                        "   \"rCode\": \"00\"\n" +
                        "}";
                break;

            case "00123":
                service = "6.1 Domestic Fund Transfer (reqId = 00123)";
                response = "{\n" +
                        "   \"message\": \"Data Transfer Berhasil Tersimpan\",\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"result\": {\n" +
                        "      \"TRFNBR\": 4\n" +
                        "   },\n" +
                        "   \"rCode\": \"00\"\n" +
                        "}";
                break;

            case "00122":
                service = "6.2 Domestic Fund Transfer Status Inquiry (reqId=00122)";
                response = "{\n" +
                        "   \"result\": {\n" +
                        "      \"trfNumber\": 4,\n" +
                        "      \"trfType\": 3,\n" +
                        "      \"outStatus\": \"0\",\n" +
                        "      \"branchSend\": \"001\",\n" +
                        "      \"trfStatus\": \"4\",\n" +
                        "      \"branchRecv\": \"001\",\n" +
                        "      \"trfDate\": \"2020-01-02\"\n" +
                        "   },\n" +
                        "   \"statusId\": 1,\n" +
                        "   \"rCode\": \"00\",\n" +
                        "   \"message\": \"INQUIRY STATUS TRANSFER BERHASIL\"\n" +
                        "}";
                break;

        }

        LinkedHashMap<String, Object> payload1 = new LinkedHashMap<>();
        payload1.put("payload", cgson);

        LinkedHashMap<String, Object> data1 = new LinkedHashMap<>();
        data1.put("service", service);
        data1.put("message_direction", req_from + "gateway");
        data1.put("message_"+apps, payload1);

        log.info(gson.toJson(data1)
                .replace("\\u003d", "=")
                .replace("\\u0026", "&") //character &
                .replace("\\u0027", "'") //character '
                .replace("\\\\", " ") //replace single backslash
        );


        ObjectMapper mapper2 = new ObjectMapper();
        Map<String,Object> map = mapper2.readValue(response, Map.class);

        LinkedHashMap<String, Object> payload2 = new LinkedHashMap<>();
        payload2.put("payload", map);

        LinkedHashMap<String, Object> data2 = new LinkedHashMap<>();
        data2.put("service", service);
        data2.put("message_direction", resp_to + "gateway");
        data2.put("message_"+apps, payload2);

        log.info(gson.toJson(data2)
                .replace("\\u003d", "=")
                .replace("\\u0026", "&") //character &
                .replace("\\u0027", "'") //character '
                .replace("\\\\", " ") //replace single backslash
        );

        return new ResponseEntity(response, HttpStatus.OK);
    }
}