/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ciberado.botserver.wms.services;

import com.ciberado.botserver.datapublishing.InfoCard;
import java.util.Set;

/**
 *
 * @author ciberado
 */
public class InfoCardServiceImpl implements InfoCardService {
    private InfoCardAttributesAccumulator primaryAccumulator;
    
    private Set<InfoCardAttributesAccumulator> secondaryAccumulators;

    public InfoCardServiceImpl(InfoCardAttributesAccumulator primaryAccumulator, 
            Set<InfoCardAttributesAccumulator> secondaryAccumulators) {
        this.primaryAccumulator = primaryAccumulator;
        this.secondaryAccumulators = secondaryAccumulators;
    }

    @Override
    public InfoCard getInfoCard(String primaryAccumulatorKeyFieldValue) {
        InfoCard infoCard = new InfoCard();
        primaryAccumulator.addAttributes(primaryAccumulatorKeyFieldValue, infoCard);
        
        for (InfoCardAttributesAccumulator accumulator : secondaryAccumulators) {
            String secondaryKeyFieldName = accumulator.getKeyFieldName();
            Object secondaryKeyFieldValue = infoCard.get(secondaryKeyFieldName);
            accumulator.addAttributes(String.valueOf(secondaryKeyFieldValue), infoCard);
        }

        return infoCard;
    }

    
    
}
