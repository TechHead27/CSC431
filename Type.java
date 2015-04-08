public abstract class Type
{
   @Override
   public boolean equals(Object o)
   {
      return o.getClass().equals(this.getClass());
   }
}
