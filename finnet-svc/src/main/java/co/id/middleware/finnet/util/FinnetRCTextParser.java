package co.id.middleware.finnet.util;

public class FinnetRCTextParser {

    public static String parse(String rc, String id) {
        String response = "";
        switch (rc.trim().toLowerCase()) {
            // General Finnet
            case"00":
                response = "Transaksi berhasil / Transaction succesful";
                break;
            case"14":
                response = "Nomor tidak ditemukan / Number not found";
                break;
            case"68":
                response = "Waktu transaksi habis / Transaction Timeout";
                break;
            case"13":
                response = "Nominal tidak valid / Invalid Denom";
                break;
            case"96":
                response = "Kerusakan sistem pada H2H / System Malfunction at H2H";
                break;
            case"31":
                response = "Bank tidak didukung oleh switch / Bank not supported by switch";
                break;
            case"94":
                response = "Duplikan STAN / Duplicate STAN";
                break;
            case"05":
                response = "Kesalahan tidak terdefinisi / Undefined error";
                break;
            case"70":
                response = "Voucher kehabisan stok / Voucher out of stock";
                break;
            case"79":
                response = "Nomor telepon diblokir / Phone number is blocked";
                break;
            case"81":
                response = "Nomor telepon tidak aktif / Phone number is expired";
                break;
            case"89":
                response = "Koneksi terputus ke penyedia tagihan / Link to billing provider is down";
                break;
            case"78":
                response = "Transaksi ditolak karena terdapat dua bill dalam satu bulan yang sama / The transaction was rejected because there were two bills in the same month";
                break;
            case"77":
                response = "Transaksi ditolak karena nama pelanggan tidak sama dalam dua bill dengan nomor telpon yang sama / Transaction rejected because customer name is not the same in two bill with same phone number";
                break;
            case"88":
                response = "Transaksi ditolak karena bill sudah terbayar di terminal atau Bank Lain / The transaction is rejected because the bill already paid in terminal or Other Bank";
                break;
            case"30":
                response = "Kesalahan format pesan / Format Message error";
                break;
            case"90":
                response = "Waktu cut off / Time cut off";
                break;
            case"40":
                response = "Jenis transaksi tidak dikenal / Unknown transaction type";
                break;
            case"12":
                response = "Belum Sign On / Not sign on";
                break;
            case"80":
                response = "Referensi tagihan tidak ditemukan / Bill reference not found";
                break;
            case"06":
                response = "Terjadi error diluar response code yang dikenal / An error occurred outside the known response code";
                break;
            case"92":
                response = "Transaksi ditolak karena nomor prefix tidak terdaftar di H2H / The transaction was rejected because the prefix number is not listed on H2H";
                break;
            case"91":
                response = "Penerbit atau switch tidak berfungsi / Issuer or switch is inoperative";
                break;

            //Case Reversal Approve
            case"21":
                response = "Maaf untuk sementara transaksi tidak dapat dilakukan / Sorry for a while transaction can not be made";
                break;

            //Case Reversal Susspend
            case"22":
                response = "Maaf untuk sementara transaksi tidak dapat dilakukan / Sorry for a while transaction can not be made";
                break;

        }
        return response;
    }
}
