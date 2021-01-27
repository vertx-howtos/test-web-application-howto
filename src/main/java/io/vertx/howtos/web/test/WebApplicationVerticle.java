package io.vertx.howtos.web.test;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class WebApplicationVerticle extends AbstractVerticle {

  final List<JsonObject> pets = new ArrayList<>(Arrays.asList(
    new JsonObject().put("id", 1).put("name", "Fufi").put("tag", "ABC"),
    new JsonObject().put("id", 2).put("name", "Garfield").put("tag", "XYZ"),
    new JsonObject().put("id", 3).put("name", "Puffa"),
    new JsonObject().put("id", 4).put("name", "Alan")
  ));

  @Override
  public void start(Promise<Void> startVerticlePromise) {
    Router router = Router.router(vertx);
    // tag::getPet[]
    router.get("/pet/:id").handler(rc -> {
      // Get the parameter id
      String unparsedId = rc.pathParam("id");
      if (unparsedId == null) {
        rc.response().setStatusCode(400).end();
        return;
      }

      // Parse the parameter id
      int id;
      try {
        id = Integer.parseInt(unparsedId);
      } catch (NumberFormatException e) {
        rc.response().setStatusCode(400).end();
        return;
      }

      // Search the pet
      Optional<JsonObject> pet = getPets(id);
      if (pet.isPresent()) {
        // Put pet id in x-pet-id header
        rc.response().putHeader("x-pet-id", String.valueOf(pet.get().getInteger("id")));
        // Reply with JSON
        rc.json(pet.get());
      } else {
        // Pet not found!
        rc.response().setStatusCode(404).end();
      }
    });
    // end::getPet[]
    // tag::addPet[]
    router.post("/pet").handler(BodyHandler.create()).handler(rc -> {
      // Do some validation... and then add the pet
      addPet(rc.getBodyAsJson());
      rc.response().setStatusCode(202).end();
    });
    // end::addPet[]
    // tag::startHttpServer[]
    vertx
      .createHttpServer()
      .requestHandler(router)
      .listen(9000)
      .<Void>mapEmpty()
      .onComplete(startVerticlePromise);
    // end::startHttpServer[]
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new WebApplicationVerticle());
  }

  private Optional<JsonObject> getPets(int id) {
    return pets.stream().filter(j -> j.getInteger("id").equals(id)).findFirst();
  }

  private void addPet(JsonObject pet) {
    this.pets.add(pet);
  }
}
