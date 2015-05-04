public class Iloc
{
	private int numArgs; // 0, 1, 2, or 3
	private String instruction;
	private String[] args = new String[3];

	public Iloc()
	{
		numArgs = 0;
		instruction = "";
	}

	public Iloc(String instruction)
	{
		numArgs = 0;
		this.instruction = instruction;
	}

	public Iloc(String instruction, String r1)
	{
		this.instruction = instruction;
		addArg(r1);
	}

	public Iloc(String instruction, String r1, String r2)
	{
		this.instruction = instruction;
		addArg(r1);
		addArg(r2);
	}

	public Iloc(String instruction, String r1, String r2, String r3)
	{
		this.instruction = instruction;
		addArg(r1);
		addArg(r2);
		addArg(r3);
	}	

	public String toString()
	{
		String ret = instruction;
		for (int i = 0; i < numArgs; i++)
			ret += (i != 0 ? "," : "") + " " +args[i];
		return ret;
	}

	public void addArg(String arg)
	{
		args[numArgs++] = arg;
	}

   public String getInst()
   {
      return instruction;
   }

   public String getReg(int i)
   {
      if (i < numArgs)
         return args[i];
      else
         return null;
   }

	//access would be iloc.args[0,1,2]

}
