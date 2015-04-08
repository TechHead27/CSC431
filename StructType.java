import java.util.HashMap;

public class StructType extends Type
{
   private HashMap<String, Type> fields;

   public StructType(HashMap<String, Type> h)
   {
      fields = h;
   }

   public Type getFieldType(String name)
   {
      Type ret = fields.get(name);
      if (ret != null && ret.getClass().equals(RecType.class))
         return this;
      else
         return ret;
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
