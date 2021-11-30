package net.blancworks.figura.models.shaders;

import net.blancworks.figura.FiguraMod;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;

import java.util.*;

public class FiguraVertexConsumerProvider implements VertexConsumerProvider {

    private ArrayList<FiguraRenderLayer> sortedLayers = new ArrayList<>();
    private HashMap<FiguraRenderLayer, BufferBuilder> bufferBuilders = new HashMap<>();
    private Map<String, FiguraRenderLayer> stringLayerMap = new HashMap<>();
    private Set<BufferBuilder> activeConsumers = new HashSet<>();
    //If this is non-null, then it will always be used. The value can be set to non-null for a short time, then reset to null after the operation.
    public FiguraRenderLayer overrideLayer = null;
    public final int maxSize;

    public FiguraVertexConsumerProvider(int maxSize) {
        this.maxSize = maxSize;
    }

    public FiguraVertexConsumerProvider() {
        this(32);
    }

    public boolean canAddLayer() {
        return bufferBuilders.size() < maxSize;
    }

    public void addLayer(FiguraRenderLayer layer) {
        //I'll print a warning, but I won't stop it from happening just in case it needs to.
        if (!canAddLayer())
            System.out.println("Warning: Adding new render layer when you're not supposed to be able to!");
        BufferBuilder bufferBuilder = new BufferBuilder(layer.getExpectedBufferSize());
        sortedLayers.add(layer);
        Collections.sort(sortedLayers);
        bufferBuilders.put(layer, bufferBuilder);
        stringLayerMap.put(layer.toString(), layer);
    }

    public void setPriority(FiguraRenderLayer layer, int newPriority) {
        layer.priority = newPriority;
        Collections.sort(sortedLayers);
    }

    public FiguraRenderLayer getLayer(String name) {
        return stringLayerMap.get(name);
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer layer) {
        if (overrideLayer != null)
            layer = overrideLayer;
        if (layer instanceof FiguraRenderLayer && bufferBuilders.containsKey(layer)) {
            BufferBuilder bufferBuilder = bufferBuilders.get(layer);
            if (activeConsumers.add(bufferBuilder))
                bufferBuilder.begin(layer.getDrawMode(), layer.getVertexFormat());
            return bufferBuilders.get(layer);
        }
        return FiguraMod.vertexConsumerProvider.getBuffer(layer);
    }

    public void draw() {
        Iterator<FiguraRenderLayer> iterator = sortedLayers.iterator();
        while(iterator.hasNext()) {
            FiguraRenderLayer layer = iterator.next();
            draw(layer);
        }
    }

    public void draw(FiguraRenderLayer layer) {
        BufferBuilder bufferBuilder = bufferBuilders.get(layer);
        if (activeConsumers.remove(bufferBuilder))
            layer.draw(bufferBuilder, 0, 0, 0);
    }

}
