package com.ia.web20.dao

import com.mongodb._
import gridfs.{GridFSDBFile, GridFS, GridFSInputFile}
import com.google.code.morphia.{Morphia}

object MongoHelper {

  // TODO mover
  private val mongoHost = "localhost"
  private val mongoPort = 27017
  private val mongoDB = "lazosDB"  
  
  val mongo: Mongo = new Mongo(new DBAddress(mongoHost, mongoPort, mongoDB))
  val db: DB = mongo.getDB(mongoDB)
  var collections = Map[String, DBCollection]()
  val gridFS = new GridFS(db)
  val morphia = new Morphia()

}