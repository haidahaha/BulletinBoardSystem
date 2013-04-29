package kr.ac.kaist.iu1nguoi.java;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class BulletinBoardSystem {
    static Scanner stdin = new Scanner(System.in);

    static Connection con = null;

    public static String login() {
        String id, pw;
        PreparedStatement pstmt = null;
        int countWrong = 0;
        while (countWrong != 3) {
            System.out.println("CS360 Simple BBS");
            System.out.println("Please type 'new' to create a new account.");
            System.out.print("Username: ");
            id = stdin.nextLine();
            if (id.equals("new")) {
                countWrong = 0;
                while (id.equals("new")) {
                    System.out.println("ID: ");
                    id = stdin.nextLine();
                    try {
                        pstmt = con.prepareStatement("SELECT * FROM users WHERE userid = ?");
                        pstmt.setString(1, id);
                        ResultSet rs = pstmt.executeQuery();
                        if (rs.next()) {
                            System.out.println("Same ID exists.");
                            id = "new";
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (pstmt != null)
                                pstmt.close();
                        } catch (Exception e) {
                        }
                    }
                }
                // else
                System.out.print("Password: ");
                pw = stdin.nextLine();
                try {
                    pstmt = con.prepareStatement("INSERT INTO users VALUES(?,?)");
                    pstmt.setString(1, id);
                    pstmt.setString(2, pw);
                    pstmt.executeUpdate();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (pstmt != null)
                            pstmt.close();
                    } catch (Exception e) {
                    }
                }
                System.out.println("New account " + id + " is created.");
            } else {
                System.out.print("Password: ");
                pw = stdin.nextLine();
                try {
                    pstmt = con.prepareStatement("SELECT * FROM users WHERE userid = ? AND passwd = ?");
                    pstmt.setString(1, id);
                    pstmt.setString(2, pw);
                    ResultSet rs = pstmt.executeQuery();
                    if (!rs.next()) {
                        System.out.println("Invalid username/password.");
                        countWrong++;
                    } else
                        return id;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (pstmt != null)
                            pstmt.close();
                    } catch (Exception e) {
                    }
                }
            }
        }
        System.out.println("Unable to log-in to Simple BBS after 3 attemps.");
        System.exit(0);
        return "";
    }

    public static void commandShow() {
        Statement stmt = null;
        ResultSet rs = null;
        int no, count;
        String writer, title;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT no, writer, count, title FROM articles ORDER BY no");

            System.out.println("CS360 Simple BBS");
            System.out.println("------------------------------------------------------------");
            System.out.printf("%-5s  %-7s   %-7s   %-6s%n", "No.", "writer", "count", "title");
            System.out.println("------------------------------------------------------------");

            while (rs.next()) {
                no = rs.getInt(1);
                writer = rs.getString(2);
                count = rs.getInt(3);
                title = rs.getString(4);
                System.out.printf("%-5d  %-7s   %-7d   %-6s%n", no, writer, count, title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (Exception e) {
            }
        }
    }

    public static void commandQuit() {
        System.out.println("Bye bye~");
        System.exit(0);
    }

    public static void commandRead() {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int i;

        System.out.println("Read Mode>>");
        System.out.print("No.: ");
        i = stdin.nextInt();
        try {
            con.setAutoCommit(false);

            pstmt = con.prepareStatement("SELECT title, content FROM articles WHERE no = ?");
            pstmt.setInt(1, i);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                String title, content;
                title = rs.getString(1);
                content = rs.getString(2);
                System.out.println("title: " + title);
                System.out.println("content: " + content);
                pstmt = con.prepareStatement("UPDATE articles SET count=count+1 WHERE no = ?");
                pstmt.setInt(1, i);
                pstmt.executeUpdate();
            } else {
                System.out.println("Article #" + i + " cannot be found.");
            }
            con.commit();
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    public static void commandWrite(String id) {
        PreparedStatement pstmt = null;
        Statement stmt = null;
        ResultSet rs = null;
        String title, content;
        int no = 1;

        System.out.println("Write Mode>>");
        System.out.print("title: ");
        title = stdin.nextLine();
        System.out.print("content: ");
        content = stdin.nextLine();

        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT MAX(no) FROM articles");
            if (rs.next())
                no = rs.getInt(1) + 1;
            pstmt = con.prepareStatement("INSERT INTO articles VALUES(?,?,0,?,?)");
            pstmt.setInt(1, no);
            pstmt.setString(2, id);
            pstmt.setString(3, title);
            pstmt.setString(4, content);
            pstmt.executeQuery();
            con.commit();
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
                if (stmt != null)
                    stmt.close();
            } catch (Exception e) {
            }
        }
    }

    public static void commandDelete(String id) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String owner;
        int no;

        System.out.println("Delete Mode>>");
        System.out.print("no.: ");
        no = stdin.nextInt();

        try {
            con.setAutoCommit(false);
            pstmt = con.prepareStatement("SELECT writer FROM articles WHERE no = ?");
            pstmt.setInt(1, no);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                owner = rs.getString(1);
                if (owner.equals(id)) {
                    pstmt = con.prepareStatement("DELETE FROM articles WHERE no=?");
                    pstmt.setInt(1, no);
                    pstmt.executeUpdate();
                    pstmt = con.prepareStatement("UPDATE articles SET no=no-1 WHERE no>?");
                    pstmt.setInt(1, no);
                    pstmt.executeUpdate();
                } else
                    System.out.println("You can't delete.");
            } else
                System.out.println("Article #" + no + " cannot be found.");
            con.commit();
        } catch (Exception e) {
            try {
                con.rollback();
            } catch (SQLException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null)
                    pstmt.close();
            } catch (Exception e) {
            }
        }
    }

    public static void command(String id) {
        char cm;
        while (true) {
            System.out.print("Command: ");
            cm = stdin.nextLine().charAt(0);
            switch (cm) {
            case 's':
                commandShow();
                break;
            case 'r':
                commandRead();
                stdin.nextLine();
                break;
            case 'w':
                commandWrite(id);
                break;
            case 'd':
                commandDelete(id);
                stdin.nextLine();
                break;
            case 'q':
                commandQuit();
                break;
            default:
                System.out.println("There is no such command. Try again!");
            	break;
            }
        }
    }

    public static void main(String[] args) {
        String id;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            con = DriverManager.getConnection(
                    "server", "username", "password");

            id = login();
            command(id);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (con != null)
                    con.close();
            } catch (Exception e) {
            }
        }
    }

}
