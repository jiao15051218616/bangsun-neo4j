/*
 * Copyright (c) 2002-2018 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.unsafe.impl.batchimport;

import org.neo4j.kernel.api.labelscan.LabelScanStore;
import org.neo4j.kernel.impl.api.CountsAccessor;
import org.neo4j.kernel.impl.store.NodeStore;
import org.neo4j.kernel.impl.util.monitoring.ProgressReporter;
import org.neo4j.unsafe.impl.batchimport.cache.NodeLabelsCache;
import org.neo4j.unsafe.impl.batchimport.staging.BatchFeedStep;
import org.neo4j.unsafe.impl.batchimport.staging.ReadRecordsStep;
import org.neo4j.unsafe.impl.batchimport.staging.Stage;
import org.neo4j.unsafe.impl.batchimport.stats.StatsProvider;

import static org.neo4j.unsafe.impl.batchimport.RecordIdIterator.allIn;
import static org.neo4j.unsafe.impl.batchimport.staging.Step.ORDER_SEND_DOWNSTREAM;
import static org.neo4j.unsafe.impl.batchimport.staging.Step.RECYCLE_BATCHES;

/**
 * Counts nodes and their labels and also builds {@link LabelScanStore label index} while doing so.
 */
public class NodeCountsAndLabelIndexBuildStage extends Stage
{
    public static final String NAME = "Node counts and label index build";

    public NodeCountsAndLabelIndexBuildStage( Configuration config, NodeLabelsCache cache, NodeStore nodeStore,
            int highLabelId, CountsAccessor.Updater countsUpdater, ProgressReporter progressReporter,
            LabelScanStore labelIndex, StatsProvider... additionalStatsProviders )
    {
        super( NAME, null, config, ORDER_SEND_DOWNSTREAM | RECYCLE_BATCHES );
        add( new BatchFeedStep( control(), config, allIn( nodeStore, config ), nodeStore.getRecordSize() ) );
        add( new ReadRecordsStep<>( control(), config, false, nodeStore ) );
        add( new LabelIndexWriterStep( control(), config, labelIndex, nodeStore ) );
        add( new RecordProcessorStep<>( control(), "COUNT", config, new NodeCountsProcessor(
                nodeStore, cache, highLabelId, countsUpdater, progressReporter ), true, additionalStatsProviders ) );
    }
}
