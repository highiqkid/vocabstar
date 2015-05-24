package org.vocabstar.service

import akka.actor._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorFlowMaterializer
import akka.stream.scaladsl._
import org.vocabstar._
import scala.concurrent.Future

trait WordService extends Actor {
  def receive: Receive
}

object WordService {
  case class UpdateWord(word: Vocabulary)
  case class RemoveWord(word: String)
  case class FindWordExact(word: String)
  case class SearchWord(word: String)
}

class InMemoryWordService extends WordService {

  import WordService._

  def receive = havingWords(Seq())

  def havingWords(words: Seq[Vocabulary]): Receive = {
    case UpdateWord(vocab) =>
      val updated = words.view.filterNot(_.word != vocab.word) :+ vocab
      sender() ! Unit
      context.become(havingWords(updated.force))
    case RemoveWord(word) =>
      val split = words.partition(_.word == word)
      sender() ! split._1.headOption
      context.become(havingWords(split._2))
    case FindWordExact(word) =>
      sender() ! words.find(_.word == word)
  }
}