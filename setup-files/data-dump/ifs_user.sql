-- MySQL dump 10.13  Distrib 5.6.24, for osx10.8 (x86_64)
--
-- Host: 127.0.0.1    Database: ifs
-- ------------------------------------------------------
-- Server version	5.6.25

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
REPLACE  IGNORE INTO `user` (`id`, `image_url`, `name`, `token`, `email`, `password`) VALUES (1,'image.jpg','Steve Smith (Lead Applicant)','123abc','applicant@innovateuk.org','b8ef180ea1440b394b71ed375a8c71365e295041fe9b6890abb2976dfd7fa0d1819c72a54a473d98'),(2,'image2.jpg','Jessica Doe (Collaborator)','456def','collaborator@innovateuk.org','b8ef180ea1440b394b71ed375a8c71365e295041fe9b6890abb2976dfd7fa0d1819c72a54a473d98'),(3,'image3.jpg','Professor Plum (Assessor)','789ghi','assessor@innovateuk.org','b8ef180ea1440b394b71ed375a8c71365e295041fe9b6890abb2976dfd7fa0d1819c72a54a473d98'),(6,'image4.jpg','Comp Exec (Competitions)','123def','competitions@innovateuk.org','b8ef180ea1440b394b71ed375a8c71365e295041fe9b6890abb2976dfd7fa0d1819c72a54a473d98'),(7,'image5.jpg','Project Finance Analyst (Finance)','123ghi','finance@innovateuk.org','b8ef180ea1440b394b71ed375a8c71365e295041fe9b6890abb2976dfd7fa0d1819c72a54a473d98');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2015-08-10 16:07:05
