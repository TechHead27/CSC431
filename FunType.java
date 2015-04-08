import java.util.ArrayList;

public class FunType extends Type
{
   private ArrayList<Type> params;
   private Type returnType;

   public FunType(ArrayList<Type> params, Type ret)
   {
      this.params = params;
      returnType = ret;
   }

   public Type getReturnType()
   {
      return returnType;
   }

   public ArrayList<Type> getParams()
   {
      return params;
   }
}
