package valar.core

object PasswordSetter {

  def main(args: Array[String]): Unit = {
    setPassword(args(0), args(1))
  }

  def setPassword(username: String, password: String): Unit = {
    implicit val db = loadDb()
    try {
      val security = new Security()
      security.setPassword(username, password)
      if (!security.check(username, password)) throw new IllegalStateException("Password doesn't match itself!")
    } finally db.close()
  }

}

