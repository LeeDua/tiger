// This is automatically generated by the Tiger compiler.
// Do NOT modify!

// structures
struct Factorial
{
  struct Factorial_vtable *vptr;
};
struct Fac
{
  struct Fac_vtable *vptr;
};
// vtables structures
struct Factorial_vtable
{
};

struct Fac_vtable
{
  int (*ComputeFac)();
};


// methods
int Fac_ComputeFac(struct Fac * this, int num)
{
  int num_aux;
  struct Fac * x_1;

  if (num < 1)
    num_aux = 1;
  else
    num_aux = num * (x_1=this, x_1->vptr->ComputeFac(x_1, num - 1));
  return num_aux;
}

// vtables
struct Factorial_vtable Factorial_vtable_ = 
{
};

struct Fac_vtable Fac_vtable_ = 
{
  Fac_ComputeFac,
};


// main method
int Tiger_main ()
{
  struct Fac * x_0;
  System_out_println ((x_0=((struct Fac*)(Tiger_new (&Fac_vtable_, sizeof(struct Fac)))), x_0->vptr->ComputeFac(x_0, 10)));
}




