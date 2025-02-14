package dataEnumerations;

public class DataTypes
{
   public static final int UINT8=1;
   public static final int INT8=2;
   public static final int UINT16=3;
   public static final int INT16=4;
   public static final int UINT32=5;
   public static final int INT32=6;
   public static final int FLT32=7;
   public static final int FLT64=8;
   public static final int HEX8=9;
   public static final int HEX16=10;
   public static final int HEX32=11;
   public static final int SWITCH=13;
   public static final int CSV_FILE=14;
   public static final int STRING_TYPE=15;
   public static final int TIME_TYPE=16;
   
   //  generic types used for displaying payload data
   //     #bits must also be specified
   public static final int UINT=101;
   public static final int INT=102;
   public static final int FLT=103;
   public static final int HEX=104;
   public static final int OP_CODE=105;
   public static final int HEX_STRING=106;
   public static final int BYTE_ARRAY=107;
   
   public static final int READ_MEM_SRC = 108;
   public static final int WRITE_MEM_DST = 108;
   
   public static final float  INFINITE_FLOAT =(float)0.99999e+38;
}
