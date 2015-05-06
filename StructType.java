import java.util.HashMap;
import java.util.ArrayList;

public class StructType extends Type
{
   private HashMap<String, Type> fields;
   private ArrayList<String> fieldOffsets;

   public StructType(HashMap<String, Type> h, ArrayList<String> stuff)
   {
      fields = h;
      fieldOffsets = stuff;
   }

   public Type getFieldType(String name)
   {
      Type ret = fields.get(name);
      if (ret != null && ret.getClass().equals(RecType.class))
         return this;
      else
         return ret;
   }

   public int getFieldOffset(String name)
   {
      return 8*fieldOffsets.indexOf(name);
   }

   public int size()
   {
      return fieldOffsets.size();
   }

   public boolean equals(Object o)
   {
      if (o.getClass().equals(NullType.class))
            return true;
      if (!o.getClass().equals(getClass()))
         return false;

      StructType other = (StructType)o;
      return fields.equals(other.fields);
   }

}
