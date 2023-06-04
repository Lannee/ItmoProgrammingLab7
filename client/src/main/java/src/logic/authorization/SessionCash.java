package src.logic.authorization;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SessionCash {

    private static final File cashFile = new File("cash/authCash");
    private static final File cashDir = new File("cash");

    public static String userNameState;
    public static String passwordState;

    static {
        try {
            if(!cashDir.exists())
                cashDir.mkdir();

            if(!cashFile.exists())
                cashFile.createNewFile();
        } catch (IOException e) {}
    }

    public static void saveSession(String userName, String password) {
        passwordState = password;
        userNameState = userName;

        try(ObjectOutputStream ous = new ObjectOutputStream(new FileOutputStream(cashFile, false))){
            List<String> toStore = new ArrayList<>(2);
            toStore.add(userName);
            toStore.add(password);
            ous.writeObject(toStore);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getPassword() {
        return passwordState;
    }

    public static String getUserName() {
        return userNameState;
    }

    public static boolean loadState() {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(cashFile))) {
            List data = (List) ois.readObject();
            userNameState = (String) data.get(0);
            passwordState = (String) data.get(1);

        } catch(Exception e) {
            return false;
        }
        return true;
    }

    public static void clearCash() {
        try {
            new FileWriter(cashFile, false).close();
        } catch (IOException e) {}
    }
}
