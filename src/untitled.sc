import scala.annotation.tailrec

def fibonacciRec(n: Int): Int = {
  @tailrec
  def loop(x: Int, a: Int = 0, b: Int = 1): Int = {
    if(x == 0) a
    else if(x == 1) b
    loop(n - 1, b, a + b)
  }
  loop(n)
}

val fibonacci = fibonacciRec(3)
println(fibonacci)