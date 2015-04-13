public class Block
{
   private ArrayList<Block> pred;
   private ArrayList<Block> succ;
   private String label;

   public Block()
   {
      pred = new ArrayList<Block>();
      succ = new ArrayList<Block>();
      label = "";
   }

   public Block(String label)
   {
      pred = new ArrayList<Block>();
      succ = new ArrayList<Block>();
      this.label = label;
   }

   public void connect(Block other)
   {
      succ.add(other);
      other.pred.add(this);
   }

   public Block[] getPred()
   {
      return pred.toArray();
   }

   public Block[] getSucc()
   {
      return succ.toArray();
   }
}
