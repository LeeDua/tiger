// This is automatically generated by the Tiger compiler.
// Do NOT modify!

// structures
struct BubbleSort
{
  struct BubbleSort_vtable *vptr;
};
struct BBS
{
  struct BBS_vtable *vptr;
  int* number;
  int size;
};
// vtables structures
struct BubbleSort_vtable
{
};

struct BBS_vtable
{
  int (*Start)();
  int (*Sort)();
  int (*Print)();
  int (*Init)();
};


// methods
int BBS_Start(struct BBS * this, int sz)
{
  int aux01;
  struct BBS * x_1;
  struct BBS * x_2;
  struct BBS * x_3;
  struct BBS * x_4;

  aux01 = (x_1=this, x_1->vptr->Init(x_1, sz));
  aux01 = (x_2=this, x_2->vptr->Print(x_2));
  System_out_println (99999);
  aux01 = (x_3=this, x_3->vptr->Sort(x_3));
  aux01 = (x_4=this, x_4->vptr->Print(x_4));
  return 0;
}
int BBS_Sort(struct BBS * this)
{
  int nt;
  int i;
  int aux02;
  int aux04;
  int aux05;
  int aux06;
  int aux07;
  int j;
  int t;

  i = this->size - 1;
  aux02 = 0 - 1;
  while(aux02 < i)
    {
      j = 1;
      while(j < i + 1)
        {
          aux07 = j - 1;
          aux04 = this->number[aux07];
          aux05 = this->number[j];
          if (aux05 < aux04)
            {
              aux06 = j - 1;
              t = this->number[aux06];
              this->number[aux06] = this->number[j];
              this->number[j] = t;
            }
          else
            nt = 0;
          j = j + 1;
        }
      i = i - 1;
    }
  return 0;
}
int BBS_Print(struct BBS * this)
{
  int j;

  j = 0;
  while(j < this->size)
    {
      System_out_println (this->number[j]);
      j = j + 1;
    }
  return 0;
}
int BBS_Init(struct BBS * this, int sz)
{

  this->size = sz;
  this->number = (int*)malloc(sizeof(int)*sz);
  this->number[0] = 20;
  this->number[1] = 7;
  this->number[2] = 12;
  this->number[3] = 18;
  this->number[4] = 2;
  this->number[5] = 11;
  this->number[6] = 6;
  this->number[7] = 9;
  this->number[8] = 19;
  this->number[9] = 5;
  return 0;
}

// vtables
struct BubbleSort_vtable BubbleSort_vtable_ = 
{
};

struct BBS_vtable BBS_vtable_ = 
{
  BBS_Start,
  BBS_Sort,
  BBS_Print,
  BBS_Init,
};


// main method
int Tiger_main ()
{
  struct BBS * x_0;
  System_out_println ((x_0=((struct BBS*)(Tiger_new (&BBS_vtable_, sizeof(struct BBS)))), x_0->vptr->Start(x_0, 10)));
}




