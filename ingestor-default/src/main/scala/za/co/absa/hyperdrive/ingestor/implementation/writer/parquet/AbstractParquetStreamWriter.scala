/*
 * Copyright 2018 ABSA Group Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package za.co.absa.hyperdrive.ingestor.implementation.writer.parquet

import org.apache.commons.configuration2.Configuration
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.apache.spark.sql.streaming.{DataStreamWriter, OutputMode, StreamingQuery}
import org.apache.spark.sql.{DataFrame, Row}
import za.co.absa.hyperdrive.ingestor.api.manager.StreamManager
import za.co.absa.hyperdrive.ingestor.api.utils.ConfigUtils.getOrThrow
import za.co.absa.hyperdrive.ingestor.api.utils.{ConfigUtils, StreamWriterUtil}
import za.co.absa.hyperdrive.ingestor.api.writer.StreamWriter
import za.co.absa.hyperdrive.shared.configurations.ConfigurationsKeys.ParquetStreamWriterKeys._

private[writer] abstract class AbstractParquetStreamWriter(destination: String, triggerProcessingTime: Option[Long],
                                                           val extraConfOptions: Map[String, String]) extends StreamWriter {
  private val logger = LogManager.getLogger
  if (StringUtils.isBlank(destination)) {
    throw new IllegalArgumentException(s"Invalid PARQUET destination: '$destination'")
  }

  override def write(dataFrame: DataFrame, streamManager: StreamManager): StreamingQuery = {
    val outStream = getOutStream(dataFrame, triggerProcessingTime)

    val streamWithOptions = addOptions(outStream, extraConfOptions)

    val streamWithOptionsAndOffset = configureOffsets(streamWithOptions, streamManager, dataFrame.sparkSession.sparkContext.hadoopConfiguration)

    logger.info(s"Writing to $destination")
    streamWithOptionsAndOffset.start(destination)
  }

  def getDestination: String = destination

  protected def getOutStream(dataFrame: DataFrame, processingTime: Option[Long]): DataStreamWriter[Row] = {
    val streamWriter = dataFrame.writeStream
    val streamWriterWithTrigger = StreamWriterUtil.configureTrigger(streamWriter, processingTime)
    streamWriterWithTrigger
      .format(source = "parquet")
      .outputMode(OutputMode.Append())
  }

  protected def addOptions(outStream: DataStreamWriter[Row], extraConfOptions: Map[String, String]): DataStreamWriter[Row] = outStream.options(extraConfOptions)

  protected def configureOffsets(outStream: DataStreamWriter[Row], streamManager: StreamManager, configuration: org.apache.hadoop.conf.Configuration): DataStreamWriter[Row] = streamManager.configure(outStream, configuration)
}

object AbstractParquetStreamWriter {

  def getDestinationDirectory(configuration: Configuration): String = getOrThrow(KEY_DESTINATION_DIRECTORY, configuration, errorMessage = s"Destination directory not found. Is '$KEY_DESTINATION_DIRECTORY' defined?")

  def getExtraOptions(configuration: Configuration): Map[String, String] = ConfigUtils.getPropertySubset(configuration, KEY_EXTRA_CONFS_ROOT)
}


