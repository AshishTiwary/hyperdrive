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

package za.co.absa.hyperdrive.ingestor.implementation.manager.factories.checkpoint

import org.apache.commons.configuration2.Configuration
import org.apache.logging.log4j.LogManager
import za.co.absa.hyperdrive.ingestor.api.manager.OffsetManager
import za.co.absa.hyperdrive.ingestor.implementation.manager.OffsetManagerFactory
import za.co.absa.hyperdrive.ingestor.implementation.manager.checkpoint.CheckpointOffsetManager
import za.co.absa.hyperdrive.shared.configurations.ConfigurationsKeys.CheckpointOffsetManagerKeys._
import za.co.absa.hyperdrive.shared.utils.ConfigUtils._

private[factories] object CheckpointOffsetManagerFactory extends OffsetManagerFactory {

  override def build(config: Configuration): OffsetManager = {
    val topic = getTopic(config)
    val checkpointBaseLocation = getCheckpointLocation(config)

    LogManager.getLogger.info(s"Going to create CheckpointOffsetManager instance using: topic='$topic', checkpoint base location='$checkpointBaseLocation'")

    new CheckpointOffsetManager(topic, checkpointBaseLocation)
  }

  private def getTopic(configuration: Configuration): String = getOrThrow(KEY_TOPIC, configuration, errorMessage = s"Could not find topic. Is '$KEY_TOPIC' defined?")

  private def getCheckpointLocation(configuration: Configuration): String = getOrThrow(KEY_CHECKPOINT_BASE_LOCATION, configuration, errorMessage = s"Could not find checkpoint base location. Is '$KEY_CHECKPOINT_BASE_LOCATION' defined?")
}
