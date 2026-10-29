package org.familycheck.senior;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import java.util.*;
public class Db extends SQLiteOpenHelper {
    public Db(Context c) {
        super(c,"familycheck.db",null,1);
    }
    public void onCreate(SQLiteDatabase d) {
        d.execSQL("CREATE TABLE cases (_id INTEGER PRIMARY KEY AUTOINCREMENT, message TEXT NOT NULL, flags TEXT NOT NULL, source TEXT NOT NULL, status TEXT NOT NULL, note TEXT NOT NULL, created INTEGER NOT NULL)");
        d.execSQL("CREATE TABLE contacts (_id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, phone TEXT NOT NULL, relationship TEXT NOT NULL, favorite INTEGER NOT NULL DEFAULT 0)");
    }
    public void onUpgrade(SQLiteDatabase d,int a,int b) {
        throw new IllegalStateException("Migration required");
    }
    public ArrayList<HashMap<String,String>> rows(String table) {
        validate(table);
        ArrayList<HashMap<String,String>> out=new ArrayList<>();
        try(Cursor c=getReadableDatabase().query(table,null,null,null,null,null,table.equals("contacts")?"favorite DESC, name COLLATE NOCASE":"created DESC")) {
            while(c.moveToNext()) {
                HashMap<String,String> r=new HashMap<>();
                for(String col:c.getColumnNames())r.put(col,c.getString(c.getColumnIndexOrThrow(col)));
                out.add(r);
            }
        }
        return out;
    }
    public HashMap<String,String> find(long id) {
        for(HashMap<String,String> r:rows("cases"))if(Long.parseLong(r.get("_id"))==id)return r;
        return null;
    }
    public long save(String message,String flags,String source) {
        ContentValues v=new ContentValues();
        v.put("message",message);
        v.put("flags",flags);
        v.put("source",source);
        v.put("status","Still unsure");
        v.put("note","");
        v.put("created",System.currentTimeMillis());
        return getWritableDatabase().insertOrThrow("cases",null,v);
    }
    public void outcome(long id,String status,String note) {
        ContentValues v=new ContentValues();
        v.put("status",status);
        v.put("note",note);
        getWritableDatabase().update("cases",v,"_id=?",new String[] {
            ""+id
        }
        );
    }
    public void contact(long id,String name,String phone,String relationship,boolean favorite) {
        ContentValues v=new ContentValues();
        v.put("name",name);
        v.put("phone",phone);
        v.put("relationship",relationship);
        v.put("favorite",favorite?1:0);
        if(id<0)getWritableDatabase().insertOrThrow("contacts",null,v);
        else getWritableDatabase().update("contacts",v,"_id=?",new String[] {
            ""+id
        }
        );
    }
    public void delete(String table,long id) {
        validate(table);
        getWritableDatabase().delete(table,"_id=?",new String[] {
            ""+id
        }
        );
    }
    private void validate(String t) {
        if(!t.equals("cases")&&!t.equals("contacts"))throw new IllegalArgumentException("Unknown table");
    }
}
