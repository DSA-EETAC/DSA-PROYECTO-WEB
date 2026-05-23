-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Versión del servidor:         12.2.2-MariaDB - MariaDB Server
-- SO del servidor:              Win64
-- HeidiSQL Versión:             12.14.0.7165
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- Volcando estructura de base de datos para templerun
CREATE DATABASE IF NOT EXISTS `templerun` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_uca1400_ai_ci */;
USE `templerun`;

-- Volcando estructura para tabla templerun.inventario
CREATE TABLE IF NOT EXISTS `inventario` (
  `user_id` int(11) DEFAULT NULL,
  `item_id` int(11) DEFAULT NULL,
  `quantity` int(11) DEFAULT NULL,
  KEY `FK_inventario_user` (`user_id`),
  KEY `FK_inventario_items` (`item_id`),
  CONSTRAINT `FK_inventario_items` FOREIGN KEY (`item_id`) REFERENCES `item` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_inventario_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Volcando datos para la tabla templerun.inventario: ~3 rows (aproximadamente)
INSERT INTO `inventario` (`user_id`, `item_id`, `quantity`) VALUES
	(6, 2, 1),
	(6, 2, 1),
	(6, 2, 1);

-- Volcando estructura para tabla templerun.item
CREATE TABLE IF NOT EXISTS `item` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `tipo` text DEFAULT NULL,
  `nombre` text DEFAULT NULL,
  `precio` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Volcando datos para la tabla templerun.item: ~3 rows (aproximadamente)
INSERT INTO `item` (`id`, `tipo`, `nombre`, `precio`) VALUES
	(1, 'Arma', 'Espada de diamante', 150),
	(2, 'Armadura', 'Botas ligeras', 50),
	(3, 'Consumible', 'Pocion de vida', 25);

-- Volcando estructura para tabla templerun.user
CREATE TABLE IF NOT EXISTS `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nombre` text DEFAULT NULL,
  `password` text DEFAULT NULL,
  `mail` text DEFAULT NULL,
  `monedas` int(11) DEFAULT NULL,
  `nivel` int(11) DEFAULT NULL,
  `ataque` int(11) DEFAULT NULL,
  `defensa` int(11) DEFAULT NULL,
  `resistencia` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_uca1400_ai_ci;

-- Volcando datos para la tabla templerun.user: ~11 rows (aproximadamente)
INSERT INTO `user` (`id`, `nombre`, `password`, `mail`, `monedas`, `nivel`, `ataque`, `defensa`, `resistencia`) VALUES
	(1, 'Juan', '1234', 'juan@gmail', NULL, NULL, NULL, NULL, NULL),
	(2, 'TPno', '*A4B6157319038724E3560894F7F932C8886EBFCF', 'juan@gmail', NULL, NULL, NULL, NULL, NULL),
	(3, 'Montes', '456', 'montes@gmail.com', NULL, NULL, NULL, NULL, NULL),
	(4, 'izan ', '589', 'izan@gmail.com', NULL, NULL, NULL, NULL, NULL),
	(5, 'izan2', '2122', 'yo@gmail.com', NULL, NULL, NULL, NULL, NULL),
	(6, 'aaron ', '1234', 'aaron@gmail.com', 150, 1, 10, 5, 5),
	(7, 'toni', '1234', 'toni@gmail.com', 500, 1, 10, 5, 5),
	(8, 'hugo ', '1234', 'hugo@gmail.com', 500, 1, 10, 5, 5),
	(9, 'izan ', '1234', 'hugo@gmail.com', 500, 1, 10, 5, 5),
	(10, 'joan', 'Templerun1234!', 'joan@gmail.com', 500, 1, 10, 5, 5),
	(11, 'aaron', 'Templerun1234!', 'aaron@gmail.com', 500, 1, 10, 5, 5);

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
