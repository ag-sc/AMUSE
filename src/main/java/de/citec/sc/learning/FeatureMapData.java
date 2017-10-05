/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.sc.learning;

/**
 *
 * @author sherzod
 */

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

public class FeatureMapData {

	final private Collection<FeatureDataPoint> dataPoints;
	final private Map<String, Integer> sparseIndexMapping;

	public FeatureMapData() {
		this.dataPoints = new HashSet<>();
		this.sparseIndexMapping = new HashMap<>();
	}

	public void addFeatureDataPoint(final FeatureDataPoint fdp) {
		this.dataPoints.add(fdp);
	}

	static public class FeatureDataPoint {

		final public Map<Integer, Double> features;
		final public double score;

		public FeatureDataPoint(FeatureMapData data, Map<String, Double> features, double score, boolean training) {
			this.features = new HashMap<>();
			for (Entry<String, Double> feature : features.entrySet()) {
				final Integer featureIndex = data.getFeatureIndex(feature.getKey(), training);

				/*
				 * Do not include features that are not present in the feature
				 * set.
				 */
				if (featureIndex == null)
					continue;

				this.features.put(featureIndex, feature.getValue());
			}
			this.score = score;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((features == null) ? 0 : features.hashCode());
			long temp;
			temp = Double.doubleToLongBits(score);
			result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			FeatureDataPoint other = (FeatureDataPoint) obj;
			if (features == null) {
				if (other.features != null)
					return false;
			} else if (!features.equals(other.features))
				return false;
			if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
				return false;
			return true;
		}

	}

	/**
	 * Returns the index of the given feature. If the feature is not presented
	 * in the feature map the index is automatically increased if the data point
	 * belongs to the training data.
	 * 
	 * @param feature
	 *            the feature
	 * @param training
	 *            if the data point belongs to training
	 * @return the index of the feature, or null if mode is prediction and the
	 *         feature is not present.
	 */
	private Integer getFeatureIndex(final String feature, final boolean training) {
		if (training)
			if (!sparseIndexMapping.containsKey(feature))
				sparseIndexMapping.put(feature, sparseIndexMapping.size());
		return sparseIndexMapping.get(feature);
	}

	public Collection<FeatureDataPoint> getDataPoints() {
		return dataPoints;
	}

	public int numberOfTotalFeatures() {
		return sparseIndexMapping.size();
	}

}
