# clustercode

Automatically convert your movies and TV shows from one file format to another using ffmpeg in a cluster.

## Features

* Scans and encodes video files from a directory and encodes them using customizable profiles.
* Encoded files are stored in an output directory.
* Major video formats supported: x264, x265 (HEVC), ...
* Take advantage of having multiple computers: Each node encodes a video, enabling parallelization.
* Works as a single node too
* No designated master. Whoever is "first" in the network, becomes master
* Supports arbiter nodes for providing a quorum. Quorums are needed to prevent a split-brain. Useful if you
have a spare Raspberry Pi or NAS that is just poor at encoding.
* Several and different cleanup strategies.
* Supports Handbrake and ffmpeg
* Basic REST API (more to come)

## Installation

* The recommended platform is Docker.
* Windows (download zip from releases tab).
* Build it using Gradle if you prefer it another way.

I hate long `docker run` commands with tons of arguments, so here is a docker-compose template:

### Docker Compose, non-swarm mode

```yaml
version: "2.2"
services:
  clustercode:
    restart: always
    image: braindoctor/clustercode:stable
    container_name: clustercode
    cpu_shares: 512
    ports:
      - "7600:7600/tcp"
    volumes:
      - "/path/to/input:/input"
      - "/path/to/output:/output"
      - "/path/to/profiles:/profiles"
# If you need modifications to the xml files, persist them:
#      - "/path/to/config:/usr/src/clustercode/config"
    environment:
    # overwrite any settings from the default using env vars!
      - CC_CLUSTER_JGROUPS_TCP_INITIAL_HOSTS=your.other.docker.node[7600],another.one[7600]
      - CC_CLUSTER_JGROUPS_EXT_ADDR=192.168.1.100
```
The external IP address is needed so that other nodes will be available to
contact the local node. Use the physical address of the docker host.

### Docker Compose, Swarm mode

**This is untested**, as I don't have a Swarm. Just make sure to limit the CPU
resources somehow, so that other containers still work reliably. I figure that
encoding is a low-priority service that takes forever anyway.
```
version: "3.2"
services:
  clustercode:
    image: braindoctor/clustercode:stable
    ports:
      - "7600:7600/tcp"
      - "7600:7600/udp"
    volumes:
      - "/path/to/input:/input"
      - "/path/to/output:/output"
      - "/path/to/profiles:/profiles"
# If you need modifications to the xml files, persist them:
#      - "/path/to/config:/usr/src/clustercode/config"
    deploy:
      restart_policy:
        condition: any
        max_attempts: 3
        window: 30s
        delay: 3s
      resources:
        limits:
          cpus: "3"
```

## Configuration

When you first start the container using docker compose, it will create a default configuration
file in `/usr/src/clustercode/config` (in the container). You can view the settings in the
`clustercode.properties` file and deviate from the default behaviour of the software. However, you should
modify the settings via Environment variables (same key/values syntax). Environment variables **always take precedence**
over the ones in `clustercode.properties`. If you made changes to the XML files, you need to mount a path from outside
in order to have them persistent.

## Project status

Active Development as of August/September 2017.

## Future Plans

- [x] Monitoring with a REST API.
- [ ] More control with REST
- [ ] [netdata](https://my-netdata.io/) plugin for monitoring.
- [x] Smooth-ier Windows deployment.
- [ ] Web-Admin (if I have spare time...)

## Docker Tags

* experimental: latest automated build of the master branch
* latest: stable build of a tagged commit from a release
* tagged: tags following the 1.x.x pattern are specific releases