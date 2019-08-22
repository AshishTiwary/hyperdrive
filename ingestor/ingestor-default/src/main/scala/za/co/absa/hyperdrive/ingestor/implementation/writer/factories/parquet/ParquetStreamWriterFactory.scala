/*
 *  Copyright 2019 ABSA Group Limited
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package za.co.absa.hyperdrive.ingestor.implementation.writer.factories.parquet

import org.apache.commons.configuration2.Configuration
import org.apache.logging.log4j.LogManager
import za.co.absa.hyperdrive.ingestor.api.writer.StreamWriter
import za.co.absa.hyperdrive.ingestor.implementation.writer.StreamWriterFactory
import za.co.absa.hyperdrive.ingestor.implementation.writer.parquet.ParquetStreamWriter
import za.co.absa.hyperdrive.shared.configurations.ConfigurationsKeys.ParquetStreamWriterKeys._
import za.co.absa.hyperdrive.shared.utils.ConfigUtils._

import scala.util.{Failure, Success, Try}

private[factories] object ParquetStreamWriterFactory extends StreamWriterFactory {

  override def build(config: Configuration): StreamWriter = {
    val destinationDirectory = getDestinationDirectory(config)
    val extraOptions = getExtraOptions(config)

    LogManager.getLogger.info(s"Going to create ParquetStreamWriter instance using: destination directory='$destinationDirectory', extra options='$extraOptions'")

    new ParquetStreamWriter(destinationDirectory, extraOptions)
  }

  private def getDestinationDirectory(configuration: Configuration): String = getOrThrow(KEY_DESTINATION_DIRECTORY, configuration, errorMessage = s"Destination directory not found. Is '$KEY_DESTINATION_DIRECTORY' defined?")

  private def getExtraOptions(configuration: Configuration): Option[Map[String,String]] = {
    import scala.collection.JavaConverters._
    val extraOptions = configuration.getKeys(KEY_EXTRA_CONFS_ROOT)
      .asScala
      .map(key => getKeyValueConf(key, configuration))

    if (extraOptions.nonEmpty) {
      Some(extraOptions.toMap)
    } else {
      None
    }
  }

  private def getKeyValueConf(key: String, configuration: Configuration): (String,String) = {
    Try(parseConf(configuration.getString(key.toString))) match {
      case Success(keyValue) => keyValue
      case Failure(exception) => throw new IllegalArgumentException(s"Invalid extra configuration for stream writer: '$key'", exception)
    }
  }

  private def parseConf(option: String): (String,String) = {
    val keyValue = option.split("=")
    if (keyValue.length == 2){
      (keyValue.head.trim(), keyValue.tail.head.trim())
    } else {
      throw new IllegalArgumentException(s"Invalid option: '$option'")
    }
  }
}
