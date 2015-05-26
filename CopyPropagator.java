import java.util.ArrayList;

public class CopyPropagator
{
   public void analyze(Block head)
   {
      boolean unchanged = false;

      for (Block b : head)
         b.calculateCopySets();

      while (!unchanged)
      {
         unchanged = true;
         for (Block b : head)
            unchanged = unchanged && b.calculateCopyIn();
      }

   }

   /*
   public void propogateCopies(Block head)
   {

   }
   */
}
